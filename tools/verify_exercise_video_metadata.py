"""Read public YouTube metadata only; no video download, playback, build or tests.
Usage: python tools/verify_exercise_video_metadata.py [catalog.json] [report.json]
Duration is taken only from the matching video's player response, never search snippets.
This script does not attest to visual technique quality or assume Shorts are short.
"""
import concurrent.futures
import datetime
import json
import re
import sys
import subprocess
import shutil
import threading
import urllib.request
from pathlib import Path

source = Path(sys.argv[1] if len(sys.argv) > 1 else 'src/main/resources/content/exercise-videos.json')
destination = Path(sys.argv[2] if len(sys.argv) > 2 else 'docs/exercise-video-duration-review.json')
decoder = json.JSONDecoder()
rate_limited = threading.Event()

def inspect(row):
    result = {'code': row['code'], 'url': row['url'], 'checkedOn': datetime.datetime.now(datetime.timezone.utc).isoformat()}
    if rate_limited.is_set():
        return dict(result, error='Skipped after YouTube rate limit; not verified', durationConfirmed=False)
    match = re.fullmatch(r'https://www\.youtube\.com/(?:watch\?v=|shorts/)([\w-]{11})', row['url'])
    if not match:
        return dict(result, error='Unsupported URL')
    try:
        request = urllib.request.Request(row['url'], headers={'User-Agent': 'Mozilla/5.0', 'Accept-Language': 'en-US,en;q=0.9'})
        try:
            with urllib.request.urlopen(request, timeout=25) as response:
                html = response.read(16000000).decode('utf-8')
        except urllib.error.URLError as error:
            if sys.platform != 'win32' or 'unknown url type: https' not in str(error):
                raise
            # Some bundled Windows Python distributions lack SSL; use Windows' TLS stack.
            if shutil.which('curl.exe'):
                html = subprocess.run(['curl.exe', '--compressed', '--connect-timeout', '8', '--max-time', '20', '--fail', '--silent', '--show-error', '--location', '--user-agent', 'Mozilla/5.0', row['url']], capture_output=True, timeout=25, check=True, encoding='utf-8-sig').stdout
            else:
                command = "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; [Console]::OutputEncoding=[Text.Encoding]::UTF8; $ErrorActionPreference='Stop'; $r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 25 -Uri '" + row['url'] + "'; [Console]::Write($r.Content)"
                html = subprocess.run(['powershell.exe', '-NoProfile', '-NonInteractive', '-Command', command], capture_output=True, timeout=35, check=True, encoding='utf-8-sig').stdout
        player = None
        for start in re.finditer(r'(?:var\s+)?ytInitialPlayerResponse\s*=\s*', html):
            try:
                value, _ = decoder.raw_decode(html[start.end():])
                if value.get('videoDetails', {}).get('videoId') == match[1]:
                    player = value
                    break
            except (ValueError, AttributeError):
                continue
        if player is None:
            raise ValueError('No matching player metadata')
        details = player['videoDetails']
        status = player.get('playabilityStatus', {})
        seconds = int(details['lengthSeconds'])
        return dict(result, videoId=details['videoId'], title=details.get('title'), channel=details.get('author'),
                    description=details.get('shortDescription'), durationSeconds=seconds,
                    playability=status.get('status'), embeddable=status.get('playableInEmbed'),
                    durationConfirmed=True, withinLimit=0 < seconds <= 59,
                    evidence='YouTube ytInitialPlayerResponse.videoDetails.lengthSeconds',
                    visualTechniqueChecked=False)
    except urllib.error.HTTPError as error:
        if error.code == 429:
            rate_limited.set()
        return dict(result, error='YouTube HTTP ' + str(error.code), durationConfirmed=False)
    except subprocess.TimeoutExpired:
        return dict(result, error='YouTube metadata request timed out', durationConfirmed=False)
    except subprocess.CalledProcessError as error:
        if '429' in str(error.stderr):
            rate_limited.set()
        return dict(result, error='Metadata download failed: ' + str(error.stderr or '')[:140], durationConfirmed=False)
    except Exception as error:
        return dict(result, error=type(error).__name__ + ': ' + str(error)[:180], durationConfirmed=False)

if __name__ == '__main__':
    rows = json.loads(source.read_text(encoding='utf-8-sig'))
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures = {pool.submit(inspect, row): row for row in rows}
        for future in concurrent.futures.as_completed(futures):
            item = future.result()
            results.append(item)
            destination.write_text(json.dumps(sorted(results, key=lambda r: r['code']), ensure_ascii=False, indent=2), encoding='utf-8')
            print(item['code'], item.get('durationSeconds', item.get('error')), flush=True)
