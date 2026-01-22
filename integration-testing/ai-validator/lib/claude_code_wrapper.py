#!/usr/bin/env python3
"""
Claude Code Wrapper - Robust utility for Claude Code CLI integration

Provides a reliable interface for calling Claude Code from Python scripts with proper
error handling, file I/O, and debugging capabilities.
"""

import subprocess
import os
import tempfile
import time
from datetime import datetime
from pathlib import Path
from typing import Dict, Any, Optional
from animated_progress import AnimatedProgress

class ClaudeCodeWrapper:
    """Robust wrapper for Claude Code CLI interactions"""
    
    def __init__(self, claude_binary_path: str = None, logs_dir: Path = None):
        if claude_binary_path is None:
            # Try to find Claude Code in PATH
            try:
                result = subprocess.run(['which', 'claude'], capture_output=True, text=True, check=True)
                self.claude_binary_path = result.stdout.strip()
            except subprocess.CalledProcessError:
                # Fallback to just 'claude' and let the system find it
                self.claude_binary_path = 'claude'
        else:
            self.claude_binary_path = claude_binary_path
        
        # Set up logs directory for debugging
        if logs_dir is None:
            # Default to current working directory logs
            self.logs_dir = Path.cwd() / "logs"
        else:
            self.logs_dir = Path(logs_dir)
        
        # Ensure logs directory exists
        self.logs_dir.mkdir(parents=True, exist_ok=True)
    
    def is_available(self) -> bool:
        """Check if Claude Code is available"""
        try:
            # Use 'claude' command directly from PATH
            result = subprocess.run(['claude', '--version'],
                         capture_output=True, check=True, timeout=10)
            return True
        except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired) as e:
            # Debug: print the actual exception for troubleshooting
            print(f"DEBUG: Claude Code availability check failed: {type(e).__name__}: {e}")
            if hasattr(e, 'stdout') and e.stdout:
                print(f"DEBUG: stdout: {e.stdout}")
            if hasattr(e, 'stderr') and e.stderr:
                print(f"DEBUG: stderr: {e.stderr}")
            return False
        except Exception as e:
            # Catch any other unexpected exceptions
            print(f"DEBUG: Unexpected exception in is_available(): {type(e).__name__}: {e}")
            return False
    
    def get_version(self) -> Optional[str]:
        """Get Claude Code version"""
        try:
            result = subprocess.run(['claude', '--version'],
                                  capture_output=True, text=True, check=True, timeout=10)
            return result.stdout.strip()
        except Exception:
            return None
    
    def analyze_from_file(self, prompt_file_path: str, output_file_path: Optional[str] = None, 
                         timeout: int = 300, use_json_output: bool = True, show_progress: bool = True, quiet: bool = False) -> Dict[str, Any]:
        """
        Analyze using Claude Code by having it read the prompt file directly
        
        Args:
            prompt_file_path: Path to file containing the prompt
            output_file_path: Optional path to save the response
            timeout: Timeout in seconds (default 300 = 5 minutes)
            use_json_output: Use --output-format json for structured responses
            show_progress: Show animated progress during execution (default True)
            quiet: Suppress logging output for cleaner JSON parsing (default False)
            
        Returns:
            Dict containing success status, response, error info, etc.
        """
        if not self.is_available():
            return {
                'success': False,
                'error': 'Claude Code is not available',
                'stderr': None,
                'response': None
            }
        
        prompt_path = Path(prompt_file_path)
        if not prompt_path.exists():
            return {
                'success': False,
                'error': f'Prompt file not found: {prompt_file_path}',
                'stderr': None,
                'response': None
            }
        
        try:
            # Build command with optional JSON output format and skip permissions
            cmd = ['claude', '-p', '--dangerously-skip-permissions']
            if not quiet:
                cmd.append('--verbose')
            if use_json_output:
                cmd.extend(['--output-format', 'json'])
            
            # Ask Claude Code to read and analyze the file directly
            file_prompt = f"Please read the file {prompt_path.absolute()} and follow the instructions contained within it."
            cmd.append(file_prompt)
            
            # Log prompt size for debugging with token estimation (only if not quiet)
            if not quiet:
                try:
                    prompt_size = prompt_path.stat().st_size
                    prompt_size_kb = prompt_size / 1024
                    # Token estimation: ~5 bytes per token or ~200 tokens per KB
                    estimated_tokens_method1 = prompt_size / 5  # 5 bytes per token
                    estimated_tokens_method2 = prompt_size_kb * 200  # 200 tokens per KB
                    
                    # Log to both stdout and add to response for debugging
                    import logging
                    logging.basicConfig(level=logging.INFO)
                    logger = logging.getLogger(__name__)
                    
                    logger.info(f"🔍 Claude Code prompt file: {prompt_path}")
                    logger.info(f"🔍 Prompt size: {prompt_size:,} bytes ({prompt_size_kb:.1f}KB)")
                    logger.info(f"🔍 Estimated tokens (5 bytes/token): {estimated_tokens_method1:,.0f}")
                    logger.info(f"🔍 Estimated tokens (200/KB): {estimated_tokens_method2:,.0f}")
                    
                    # Show if this is likely to cause issues (Claude supports ~200K tokens)
                    if prompt_size_kb > 400:  # ~80K tokens at 200 tokens/KB
                        logger.warning(f"⚠️  Large prompt detected - may cause Claude Code processing issues")
                    if estimated_tokens_method1 > 150000:  # Getting close to 200K limit
                        logger.warning(f"⚠️  Prompt approaching Claude's 200K token limit")
                        
                except Exception as e:
                    print(f"🔍 Could not get prompt size: {e}")
            
            # Call Claude Code with optional progress animation
            try:
                start_time = time.time()
                if not quiet:
                    logger.info(f"🔍 Starting Claude Code execution...")
                
                # Start progress animation if requested
                progress = None
                if show_progress and not quiet:
                    progress = AnimatedProgress("🧠 Claude Code analyzing")
                    progress.start()
                
                try:
                    # Use Popen for non-blocking execution with progress
                    process = subprocess.Popen(
                        cmd,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        text=True
                    )
                    
                    # Wait for completion with timeout
                    stdout, stderr = process.communicate(timeout=timeout)
                    returncode = process.returncode
                    
                finally:
                    # Always stop progress animation
                    if progress:
                        progress.stop()
                
                end_time = time.time()
                duration = end_time - start_time
                if not quiet:
                    logger.info(f"🔍 Claude Code completed in {duration:.1f} seconds")
                
                # Check if command succeeded
                if returncode != 0:
                    raise subprocess.CalledProcessError(returncode, cmd, stdout, stderr)
                
            except subprocess.TimeoutExpired:
                return {
                    'success': False,
                    'error': f'Claude Code analysis timed out after {timeout} seconds',
                    'stderr': None,
                    'response': None
                }
            except subprocess.CalledProcessError as e:
                return {
                    'success': False,
                    'error': f'Claude Code failed with return code {e.returncode}',
                    'stderr': e.stderr,
                    'response': None
                }
            
            # Save output if requested
            if output_file_path:
                output_path = Path(output_file_path)
                output_path.parent.mkdir(parents=True, exist_ok=True)
                with open(output_path, 'w', encoding='utf-8') as f:
                    f.write(f"Return code: {returncode}\n")
                    f.write(f"Stdout length: {len(stdout)}\n")
                    f.write(f"Stderr: {stderr or 'None'}\n")
                    f.write(f"Response:\n{stdout}")
            
            # Process the response based on output format
            if use_json_output:
                # Extract actual content from Claude Code JSON response
                try:
                    import json
                    claude_response = json.loads(stdout)
                    
                    # Check if this is an error response
                    if isinstance(claude_response, dict) and claude_response.get('subtype') == 'error_during_execution':
                        return {
                            'success': False,
                            'error': 'Claude Code execution error - likely timeout or processing failure',
                            'stderr': stderr,
                            'response': None
                        }
                    
                    # Extract actual content and cost information from Claude Code JSON response
                    actual_content = None
                    cost_info = {}
                    
                    try:
                        # Debug logging to see what we're working with
                        import logging
                        logger = logging.getLogger(__name__)
                        logger.info(f"Claude response type: {type(claude_response)}")
                        if isinstance(claude_response, list):
                            logger.info(f"List length: {len(claude_response)}")
                            if len(claude_response) > 0:
                                logger.info(f"First item type: {type(claude_response[0])}")
                        
                        # Handle array format (conversation messages)
                        if isinstance(claude_response, list):
                            # Find the last assistant message with content
                            for i, message in enumerate(reversed(claude_response)):
                                logger.info(f"Processing message {i}: type={type(message)}")
                                if not isinstance(message, dict):
                                    continue
                                logger.info(f"Message keys: {message.keys()}")
                                if message.get('type') == 'assistant':
                                    msg_data = message.get('message', {})
                                    logger.info(f"Message data type: {type(msg_data)}")
                                    if isinstance(msg_data, dict) and 'content' in msg_data:
                                        content_list = msg_data['content']
                                        logger.info(f"Content list type: {type(content_list)}")
                                        if isinstance(content_list, list) and len(content_list) > 0:
                                            # Get text content from the last content item
                                            last_content = content_list[-1]
                                            logger.info(f"Last content type: {type(last_content)}")
                                            if isinstance(last_content, dict) and 'text' in last_content:
                                                actual_content = last_content['text']
                                                logger.info(f"Found content: {actual_content[:100] if actual_content else 'None'}...")
                                                break
                                                
                                # Extract cost information from usage metadata
                                elif message.get('type') == 'usage':
                                    usage_data = message.get('usage', {})
                                    if usage_data:
                                        cost_info.update({
                                            'input_tokens': usage_data.get('input_tokens'),
                                            'output_tokens': usage_data.get('output_tokens'),
                                            'total_tokens': usage_data.get('input_tokens', 0) + usage_data.get('output_tokens', 0),
                                            'cache_creation_input_tokens': usage_data.get('cache_creation_input_tokens'),
                                            'cache_read_input_tokens': usage_data.get('cache_read_input_tokens')
                                        })
                                        logger.info(f"Found usage info: {cost_info}")
                        
                        # Handle single object format
                        elif isinstance(claude_response, dict):
                            if 'result' in claude_response:
                                actual_content = claude_response['result']
                            elif 'content' in claude_response:
                                actual_content = claude_response['content']
                            elif 'response' in claude_response:
                                actual_content = claude_response['response']
                            
                            # Extract usage/cost information from single object format
                            if 'usage' in claude_response:
                                usage_data = claude_response['usage']
                                cost_info.update({
                                    'input_tokens': usage_data.get('input_tokens'),
                                    'output_tokens': usage_data.get('output_tokens'),
                                    'total_tokens': usage_data.get('input_tokens', 0) + usage_data.get('output_tokens', 0),
                                    'cache_creation_input_tokens': usage_data.get('cache_creation_input_tokens'),
                                    'cache_read_input_tokens': usage_data.get('cache_read_input_tokens')
                                })
                                logger.info(f"Found usage info in single object: {cost_info}")
                        
                    except Exception as parse_error:
                        # Log the parsing error but continue with fallback
                        import logging
                        logger = logging.getLogger(__name__)
                        logger.error(f"Error parsing Claude Code JSON response: {parse_error}")
                        import traceback
                        logger.error(f"Traceback: {traceback.format_exc()}")
                    
                    # If we couldn't extract content, fallback to raw JSON as string
                    if actual_content is None:
                        actual_content = json.dumps(claude_response) if isinstance(claude_response, (dict, list)) else stdout
                    
                    # Add execution duration to cost info
                    cost_info['duration_seconds'] = duration
                    
                    return {
                        'success': True,
                        'response': actual_content,
                        'output_file': output_file_path,
                        'stderr': stderr,
                        'error': None,
                        'cost_info': cost_info if cost_info else None
                    }
                    
                except json.JSONDecodeError:
                    # If it's not valid JSON, return as-is (probably plain text response)
                    return {
                        'success': True,
                        'response': stdout,
                        'output_file': output_file_path,
                        'stderr': stderr,
                        'error': None,
                        'cost_info': {'duration_seconds': duration}
                    }
            else:
                # Plain text response
                return {
                    'success': True,
                    'response': stdout,
                    'output_file': output_file_path,
                    'stderr': stderr,
                    'error': None,
                    'cost_info': {'duration_seconds': duration}
                }
            
        except Exception as e:
            stderr_val = stderr if 'stderr' in locals() else None
            return {
                'success': False,
                'error': f'Unexpected error: {str(e)}',
                'stderr': stderr_val,
                'response': None
            }
    
    def extract_json_from_response(self, text_content: str) -> Optional[Dict[str, Any]]:
        """
        Extract JSON from Claude Code response text using multiple fallback strategies.
        
        This centralizes all JSON extraction logic that was previously duplicated
        across backport_assessor.py, solution_assessor.py, ai_conversation_analyzer.py, etc.
        
        Args:
            text_content: Raw text content from Claude Code response
            
        Returns:
            Parsed JSON dict if successful, None if extraction fails
        """
        import re
        import json
        import logging
        
        logger = logging.getLogger(__name__)
        
        if not text_content:
            logger.warning("🔍 extract_json_from_response: Empty text content provided")
            return None
        
        logger.info(f"🔍 Extracting JSON from response ({len(text_content)} chars)")
        
        # Strategy 1: Try to extract JSON from markdown code block
        try:
            json_pattern = r'```json\s*\n(.*?)\n```'
            json_match = re.search(json_pattern, text_content, re.DOTALL)
            
            if json_match:
                json_str = json_match.group(1).strip()
                logger.info(f"🔍 Found JSON in markdown code block ({len(json_str)} chars)")
                
                try:
                    parsed_json = json.loads(json_str)
                    logger.info("✅ Successfully parsed JSON from markdown code block")
                    return parsed_json
                except json.JSONDecodeError as e:
                    logger.warning(f"⚠️  JSON code block parse error: {e}")
                    # Continue to next strategy
        except Exception as e:
            logger.warning(f"⚠️  Markdown code block extraction failed: {e}")
        
        # Strategy 2: Try direct JSON parsing (look for { ... } blocks)
        try:
            logger.info("🔍 Trying direct JSON parsing from response...")
            lines = text_content.split('\n')
            json_lines = []
            in_json = False
            brace_count = 0
            
            for line in lines:
                line_stripped = line.strip()
                
                # Start JSON block detection
                if line_stripped.startswith('{') and not in_json:
                    in_json = True
                    brace_count = 0
                    json_lines = [line]  # Reset and start fresh
                elif in_json:
                    json_lines.append(line)
                
                # Count braces to handle nested objects
                if in_json:
                    brace_count += line.count('{') - line.count('}')
                    
                    # End of JSON block
                    if brace_count == 0 and line_stripped.endswith('}'):
                        break
            
            if json_lines and in_json:
                json_str = '\n'.join(json_lines).strip()
                logger.info(f"🔍 Found direct JSON block ({len(json_str)} chars)")
                
                try:
                    parsed_json = json.loads(json_str)
                    logger.info("✅ Successfully parsed JSON from direct parsing")
                    return parsed_json
                except json.JSONDecodeError as e:
                    logger.warning(f"⚠️  Direct JSON parse error: {e}")
                    logger.warning(f"⚠️  Problematic JSON preview: {json_str[:200]}...")
                    # Continue to next strategy
        except Exception as e:
            logger.warning(f"⚠️  Direct JSON parsing failed: {e}")
        
        # Strategy 3: Try to find JSON using simple bracket matching
        try:
            logger.info("🔍 Trying bracket-based JSON extraction...")
            
            # Find first { and last }
            first_brace = text_content.find('{')
            last_brace = text_content.rfind('}')
            
            if first_brace != -1 and last_brace != -1 and last_brace > first_brace:
                json_str = text_content[first_brace:last_brace+1].strip()
                logger.info(f"🔍 Found bracket-based JSON ({len(json_str)} chars)")
                
                try:
                    parsed_json = json.loads(json_str)
                    logger.info("✅ Successfully parsed JSON using bracket matching")
                    return parsed_json
                except json.JSONDecodeError as e:
                    logger.warning(f"⚠️  Bracket-based JSON parse error: {e}")
        except Exception as e:
            logger.warning(f"⚠️  Bracket-based JSON extraction failed: {e}")
        
        # All strategies failed
        logger.error("❌ All JSON extraction strategies failed")
        logger.error(f"❌ Text preview: {text_content[:500]}...")
        return None
    
    def analyze_from_file_with_json(self, prompt_file_path: str, output_file_path: Optional[str] = None,
                                   timeout: int = 300, show_progress: bool = True, quiet: bool = False) -> Dict[str, Any]:
        """
        Analyze from file and automatically extract JSON from the response.
        
        This is a convenience method that combines analyze_from_file() with JSON extraction.
        
        Args:
            prompt_file_path: Path to the prompt file
            output_file_path: Optional path to save the response
            timeout: Timeout in seconds
            show_progress: Show animated progress
            quiet: Suppress logging output for cleaner JSON parsing
            
        Returns:
            Dict with keys: success, response (raw text), json_data (parsed), error, etc.
        """
        # Get raw response using JSON output mode
        result = self.analyze_from_file(
            prompt_file_path=prompt_file_path,
            output_file_path=output_file_path,
            timeout=timeout,
            use_json_output=True,  # Force JSON mode for better parsing
            show_progress=show_progress,
            quiet=quiet
        )
        
        # If the call failed, return as-is
        if not result.get('success'):
            return result
        
        # Extract JSON from the response
        raw_response = result.get('response', '')
        json_data = self.extract_json_from_response(raw_response)
        
        # Add JSON data to the result
        result['json_data'] = json_data
        result['json_extraction_success'] = json_data is not None
        
        return result
    
    def analyze_from_text_with_json(self, prompt_text: str, output_file_path: Optional[str] = None,
                                   timeout: int = 300, show_progress: bool = True, quiet: bool = False) -> Dict[str, Any]:
        """
        Analyze from text and automatically extract JSON from the response.
        
        This is a convenience method that combines analyze_from_text() with JSON extraction.
        
        Args:
            prompt_text: The prompt text to analyze
            output_file_path: Optional path to save the response
            timeout: Timeout in seconds
            show_progress: Show animated progress
            quiet: Suppress logging output for cleaner JSON parsing
            
        Returns:
            Dict with keys: success, response (raw text), json_data (parsed), error, etc.
        """
        # Get raw response using JSON output mode
        result = self.analyze_from_text(
            prompt_text=prompt_text,
            output_file_path=output_file_path,
            timeout=timeout,
            use_json_output=True,  # Force JSON mode for better parsing
            show_progress=show_progress,
            quiet=quiet
        )
        
        # If the call failed, return as-is
        if not result.get('success'):
            return result
        
        # Extract JSON from the response
        raw_response = result.get('response', '')
        json_data = self.extract_json_from_response(raw_response)
        
        # Add JSON data to the result
        result['json_data'] = json_data
        result['json_extraction_success'] = json_data is not None
        
        return result
    
    def analyze_from_text(self, prompt_text: str, output_file_path: Optional[str] = None,
                         timeout: int = 300, use_json_output: bool = True, show_progress: bool = True, quiet: bool = False) -> Dict[str, Any]:
        """
        Analyze using Claude Code with prompt text directly
        
        Args:
            prompt_text: The prompt text to analyze
            output_file_path: Optional path to save the response
            timeout: Timeout in seconds (default 300 = 5 minutes)
            use_json_output: Use --output-format json for structured responses
            show_progress: Show animated progress during execution (default True)
            quiet: Suppress logging output for cleaner JSON parsing (default False)
            
        Returns:
            Dict containing success status, response, error info, etc.
        """
        # Create persistent prompt file for debugging
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        prompt_file_path = self.logs_dir / f"claude-prompt-{timestamp}.md"
        
        with open(prompt_file_path, 'w', encoding='utf-8') as f:
            f.write(prompt_text)
        
        temp_file_path = str(prompt_file_path)
        
        try:
            # Use the file-based analysis
            result = self.analyze_from_file(temp_file_path, output_file_path, timeout, use_json_output, show_progress, quiet)
            # Keep the prompt file for debugging - don't delete it
            return result
        except Exception as e:
            # Log error but keep prompt file for debugging
            import logging
            logger = logging.getLogger(__name__)
            logger.error(f"Error in analyze_from_text, prompt saved to: {prompt_file_path}")
            raise


# Test function when run directly
if __name__ == "__main__":
    """Test the ClaudeCodeWrapper with the saved prompt"""
    claude = ClaudeCodeWrapper()
    
    print("Claude Code Wrapper Test")
    print("=" * 40)
    
    if claude.is_available():
        version = claude.get_version()
        print(f"✅ Claude Code available: {version}")
        
        # Test with the saved prompt
        prompt_file = '/home/mark/project-mgmt/spring-ai-project-mgmt/pr-review/logs/claude-prompt-ai-analyzer.txt'
        output_file = '/home/mark/project-mgmt/spring-ai-project-mgmt/pr-review/logs/claude-response-wrapper-test.txt'
        
        if Path(prompt_file).exists():
            print(f"🧪 Testing with prompt file: {prompt_file}")
            result = claude.analyze_from_file(prompt_file, output_file)
            
            if result['success']:
                print("✅ Analysis completed successfully!")
                print(f"📁 Output saved to: {result['output_file']}")
                print(f"📊 Response length: {len(result['response'])} chars")
                print(f"🔍 Response preview: {result['response'][:200]}...")
            else:
                print(f"❌ Error: {result['error']}")
                if result.get('stderr'):
                    print(f"stderr: {result['stderr']}")
        else:
            print(f"❌ Prompt file not found: {prompt_file}")
    else:
        print("❌ Claude Code is not available")