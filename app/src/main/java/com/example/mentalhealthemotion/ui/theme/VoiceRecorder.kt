package com.example.mentalhealthemotion.ui.theme

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            outputFile = File(context.getExternalFilesDir(null), "voice_recording_${System.currentTimeMillis()}.mp4")
            setOutputFile(outputFile?.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Failed to start recording: ${e.message}")
            }
        }
    }

    fun stopRecording(onRecordingComplete: (Uri) -> Unit) {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                outputFile?.let { file ->
                    onRecordingComplete(Uri.fromFile(file))
                }
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Failed to stop recording: ${e.message}")
            }
        }
        mediaRecorder = null
    }
}