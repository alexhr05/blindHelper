import os
import time
from detect import run

folder_path = 'data/images'

while True:
  if not os.listdir(folder_path):
    #empty
    print("empty")
  else:
    run() #not empty

    # deletes all files in the folder

    # for file in os.listdir(folder_path):
    #   os.remove(os.path.join(folder_path, file))

  time.sleep(1)  # check every 1 second