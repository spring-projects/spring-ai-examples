package com.example.transcription;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.qxotic.jinfer.Transcription;
import com.qxotic.jinfer.spring.ai.JinferTranscriptionModel;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
class TranscriptionController {

    record TranscriptResponse(String text, long elapsedMillis) {}

    record WordTiming(String text, double startSeconds, double endSeconds, double confidence) {}

    record DetailedTranscriptResponse(String text, List<WordTiming> words, double audioSeconds,
            long elapsedMillis, double realTimeFactor) {}

    private final TranscriptionModel transcriptionModel;

    private final JinferTranscriptionModel jinferTranscriptionModel;

    // Both parameters are the same auto-configured bean, seen through the portable Spring AI interface
    // and through the jinfer type that adds word timings.
    TranscriptionController(TranscriptionModel transcriptionModel, JinferTranscriptionModel jinferTranscriptionModel) {
        this.transcriptionModel = transcriptionModel;
        this.jinferTranscriptionModel = jinferTranscriptionModel;
    }

    /** Portable Spring AI API: works unchanged with any TranscriptionModel provider. */
    @PostMapping("/transcribe")
    TranscriptResponse transcribe(@RequestParam("file") MultipartFile file) {
        long start = System.nanoTime();
        String text = this.transcriptionModel.transcribe(file.getResource());
        return new TranscriptResponse(text, millisSince(start));
    }

    /** jinfer-specific API: the full Transcription, with per-word time spans and confidences. */
    @PostMapping("/transcribe/words")
    DetailedTranscriptResponse transcribeWithWords(@RequestParam("file") MultipartFile file) throws IOException {
        long start = System.nanoTime();
        Transcription transcription = this.jinferTranscriptionModel.transcribe(file.getBytes());
        long elapsed = millisSince(start);

        List<WordTiming> words = transcription.words()
            .stream()
            .map(w -> new WordTiming(w.text(), seconds(w.start()), seconds(w.end()), round(w.confidence())))
            .toList();
        // the last token's end is the speech end, a lower bound of the audio length
        double audioSeconds = transcription.tokens().isEmpty() ? 0
                : seconds(transcription.tokens().getLast().end());
        double realTimeFactor = elapsed == 0 ? 0 : round(audioSeconds * 1000 / elapsed);
        return new DetailedTranscriptResponse(transcription.text(), words, audioSeconds, elapsed, realTimeFactor);
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static double seconds(Duration d) {
        return round(d.toNanos() / 1e9);
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
