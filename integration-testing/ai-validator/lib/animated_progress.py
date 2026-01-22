#!/usr/bin/env python3
"""
Simple ASCII Animation Progress Indicator for Claude Code Operations

Provides a clean spinner animation during long-running Claude Code operations
to give visual feedback that the process is working.
"""

import threading
import time
import sys
from typing import Optional


class AnimatedProgress:
    """Simple spinner animation for long-running operations"""
    
    # Braille spinner - looks professional and works on most terminals
    SPINNER_FRAMES = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"]
    
    def __init__(self, message: str = "🧠 Thinking", update_interval: float = 0.5):
        """
        Initialize the progress animation
        
        Args:
            message: Base message to show (e.g. "🧠 Thinking")
            update_interval: How often to update animation (seconds)
        """
        self.message = message
        self.update_interval = update_interval
        self.stop_event = threading.Event()
        self.animation_thread: Optional[threading.Thread] = None
        self.frame_index = 0
        
    def start(self):
        """Start the animation in a background thread"""
        if self.animation_thread and self.animation_thread.is_alive():
            return  # Already running
            
        self.stop_event.clear()
        self.frame_index = 0
        self.animation_thread = threading.Thread(target=self._animate, daemon=True)
        self.animation_thread.start()
        
    def stop(self):
        """Stop the animation and clean up"""
        if self.stop_event:
            self.stop_event.set()
            
        if self.animation_thread and self.animation_thread.is_alive():
            self.animation_thread.join(timeout=1.0)
            
        # Clear the line
        sys.stdout.write('\r' + ' ' * 50 + '\r')
        sys.stdout.flush()
        
    def _animate(self):
        """Internal animation loop - runs in background thread"""
        start_time = time.time()
        
        while not self.stop_event.is_set():
            # Get current spinner frame
            frame = self.SPINNER_FRAMES[self.frame_index % len(self.SPINNER_FRAMES)]
            
            # Calculate elapsed time
            elapsed = time.time() - start_time
            elapsed_str = f"{elapsed:.1f}s"
            
            # Build the progress line
            progress_line = f"\r{frame} {self.message}... {elapsed_str}"
            
            # Write without newline and flush immediately
            sys.stdout.write(progress_line)
            sys.stdout.flush()
            
            # Wait for next frame or stop signal
            if self.stop_event.wait(self.update_interval):
                break
                
            self.frame_index += 1
            
    def __enter__(self):
        """Context manager entry"""
        self.start()
        return self
        
    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit"""
        self.stop()


def test_animation():
    """Test the animation for a few seconds"""
    print("Testing animated progress...")
    
    with AnimatedProgress("🧠 Claude Code analyzing") as progress:
        time.sleep(10)  # Simulate a long operation
        
    print("✅ Animation test completed!")


if __name__ == "__main__":
    test_animation()