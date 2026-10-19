# jinfer transcription demo

A Spring Boot + Spring AI application that converts speech to text with
[NVIDIA Parakeet](https://huggingface.co/nvidia/parakeet-tdt-0.6b-v3). The model runs **inside the
application's JVM** through [Qxotic jinfer](https://qxotic.ai): there is no cloud service, no API key,
no Python and no whisper.cpp. The audio never leaves the machine.

It includes a small web page where you can record from the microphone or upload a file, and see the
transcript with per-word timings and confidences.

## Run it

Requirements: **JDK 25** and Maven 3.9+. The example uses Spring Boot 4.1.1 and Spring AI 2.1.0-SNAPSHOT.

```bash
sdk use java 25.0.3-graal
mvn spring-boot:run
```

Then open <http://localhost:8080>.

The first start downloads `mudler/parakeet-cpp-gguf/tdt-0.6b-v3-q8_0.gguf` (897 MB) into
`~/Library/Caches/jinfer` on macOS or `~/.cache/jinfer` on Linux. Later starts take a few seconds.

## Endpoints

| Endpoint | API used | Returns |
|----------|----------|---------|
| `POST /api/transcribe` (multipart `file`) | Portable Spring AI `TranscriptionModel` | The text and the elapsed time |
| `POST /api/transcribe/words` (multipart `file`) | jinfer's `JinferTranscriptionModel.transcribe(byte[])` | The text, every word with its start and end time and confidence, and the speed relative to real time |

The two endpoints use the same auto-configured bean. `/api/transcribe` works unchanged with any Spring AI
transcription provider. `/api/transcribe/words` uses jinfer's richer `Transcription` result.

```bash
# make a test recording on macOS (or use any WAV / MP3 file)
say -o speech.wav --data-format=LEI16@16000 "Spring AI now runs speech recognition locally."

curl -F file=@speech.wav localhost:8080/api/transcribe
# {"text":"Spring AI now runs speech recognition locally.","elapsedMillis":412}

curl -F file=@speech.wav localhost:8080/api/transcribe/words
# {"text":"...","words":[{"text":"Spring","startSeconds":0.0,"endSeconds":0.24,"confidence":1.0}, ...],
#  "audioSeconds":8.72,"elapsedMillis":942,"realTimeFactor":9.26}
```

On an Apple Silicon laptop, an 8.7-second clip transcribes in under a second, about 9x faster than real
time.

## Audio formats

jinfer decodes audio locally to 16 kHz mono:

- WAV, AIFF and AU are decoded by `javax.sound`. No extra tools are needed, and any sample rate works.
- Compressed formats such as MP3, M4A, OGG and WebM fall back to `ffmpeg`, which must be on the `PATH`.
- Audio is limited to 60 minutes by default. Raise the limit with `-Djinfer.codecs.maxAudioMinutes=...`.

The web page records the microphone as 16 kHz 16-bit mono WAV in the browser, so recording works
without `ffmpeg`. Browsers allow microphone access on `localhost`. For any other host, serve the page
over HTTPS.

## Choosing a model

Set `spring.ai.jinfer.transcription.model`, or use an environment variable:

```bash
JINFER_TRANSCRIPTION_MODEL=mudler/parakeet-cpp-gguf/tdt_ctc-110m-q8_0.gguf mvn spring-boot:run
```

| Model | Languages | Notes |
|-------|-----------|-------|
| `tdt-0.6b-v3-q8_0.gguf` (default) | 25 European languages, detected automatically | A good balance of quality and speed |
| `tdt-0.6b-v3-q4_k.gguf` | Same | Fastest 0.6B variant |
| `tdt-0.6b-v2-q8_0.gguf` | English | |
| `tdt_ctc-110m-q8_0.gguf` | English | Tiny: transcribes an hour of speech in under a minute |

All checkpoints are in [mudler/parakeet-cpp-gguf](https://huggingface.co/mudler/parakeet-cpp-gguf).

## How it is wired

```xml
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jinfer-spring-ai-spring-boot-starter</artifactId>
</dependency>
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jinfer-parakeet</artifactId>   <!-- the Parakeet architecture -->
</dependency>
```

```properties
spring.ai.jinfer.transcription.model=mudler/parakeet-cpp-gguf/tdt-0.6b-v3-q8_0.gguf
```

Setting the model property is what creates the `TranscriptionModel` bean. The pom also imports the
`spring-ai-bom` (2.1.0-SNAPSHOT) and `jinfer-bom` (0.3.1). It passes
`--add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED` to the JVM through the
`spring-boot-maven-plugin`. To run the jar directly, pass the same flags:

```bash
mvn package
java --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED \
     -jar target/jinfer-transcription-demo-0.0.1-SNAPSHOT.jar
```

`JinferTranscriptionModel` is thread-safe: concurrent requests transcribe in parallel.
