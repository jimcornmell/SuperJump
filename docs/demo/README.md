# Start server

```shell
docker run -d \
  --name kokoro-tts \
  --restart unless-stopped \
  -p 7860:7860 \
  -v kokorotts_data:/app/persistent \
  hangrylabs/kokorotts:v1.0
```

## Navigate to UI

http://localhost:7860

# Record Video

1. Open OBS studio
    * Use a 16:9 aspect ratio (1920×1080).
    * In settings set resolutions to match.
2. Open KeysClicks
3. In IDE
   * Increate size to 125%
   * Switch on `Presentation Assistance`
   * Open [EXAMPLE.md](../../EXAMPLE.md)
   * Open [EXAMPLE.sh](../../EXAMPLE.sh)
   * Anonymise settings to defaults
4. In browse:
   * Open: https://plugins.jetbrains.com/search?excludeTags=theme&products=idea&search=superjump
   * Logout of marketplace.
5. Open audio.
6. Wait for YouTube to finish processing HD before adding it to Marketplace.

# Replace audio

## Mute or strip the original audio track from the file:

```bash
cd docs/demo
```
```bash
ffmpeg -i VideoScript.mp4 -an -vcodec copy video_no_audio.mp4
```

## Combine the video with your new replacement audio track:

```bash
ffmpeg -i video_no_audio.mp4 -i VideoScript.mp3 -c:v copy -c:a aac VideoScriptNew.mp4
```

# Link
Copy link of the following form, this gives 1080p option in players.

The video: https://www.youtube.com/watch?v=YT_ID
The thumbnail: https://img.youtube.com/vi/YT_ID/0.jpg
