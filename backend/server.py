import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from requests_toolbelt.multipart import decoder
import numpy as np
import cv2
import os
import sys
from pathlib import Path
from getPosition import get_position
import time
from urllib.parse import urlparse
from resultEN import messageEN
from resultBG import messageBG

import torch

FILE = Path(__file__).resolve()
ROOT = FILE.parents[0]  # YOLOv5 root directory
if str(ROOT) not in sys.path:
    sys.path.append(str(ROOT))  # add ROOT to PATH
ROOT = Path(os.path.relpath(ROOT, Path.cwd()))  # relative

from models.common import DetectMultiBackend
from utils.augmentations import (letterbox)
from utils.general import (Profile, check_img_size, non_max_suppression, scale_boxes, strip_optimizer, xyxy2xywh)
from utils.torch_utils import select_device, smart_inference_mode

hostName = "127.0.0.1"
serverPort = 8080

#INItialization

conf_thres=0.25,  # confidence threshold
iou_thres=0.45,  # NMS IOU threshold
max_det=1000,  # maximum detections per image
classes=None,  # filter by class: --class 0, or --class 0 2 3
agnostic_nms=False,  # class-agnostic NMS
augment=False,  # augmented inference
update=False,  # update all models

# Load model
weights='yolov5s.pt'  # model path or triton URL
data='data/coco128.yaml'  # dataset.yaml path
imgsz=(640, 640)  # inference size (height, width)
half=False  # use FP16 half-precision inference
dnn=False  # use OpenCV DNN for ONNX inference
device=''  # cuda device, i.e. 0 or 0,1,2,3 or cpu

device = select_device(device)
model = DetectMultiBackend(weights, device=device, dnn=dnn, data=data, fp16=half)
stride, names, pt = model.stride, model.names, model.pt
imgsz = check_img_size(imgsz, s=stride)  # check image size

def detect(
        image,
        word,
        lang,
        weights=ROOT / 'yolov5s.pt',  # model path or triton URL
        data=ROOT / 'data/coco128.yaml',  # dataset.yaml path
        imgsz=(640, 640),  # inference size (height, width)
        conf_thres=0.25,  # confidence threshold
        iou_thres=0.45,  # NMS IOU threshold
        max_det=1000,  # maximum detections per image
        device='',  # cuda device, i.e. 0 or 0,1,2,3 or cpu
        classes=None,  # filter by class: --class 0, or --class 0 2 3
        agnostic_nms=False,  # class-agnostic NMS
        augment=False,  # augmented inference
        update=False,  # update all models
        half=False,  # use FP16 half-precision inference
        dnn=False,  # use OpenCV DNN for ONNX inference
):
    responseArray = []
    # Load model
    device = select_device(device)
    model = DetectMultiBackend(weights, device=device, dnn=dnn, data=data, fp16=half)
    stride, names, pt = model.stride, model.names, model.pt
    imgsz = check_img_size(imgsz, s=stride)  # check image size
    if True:

        # cv2.imshow('image', image)
        # cv2.waitKey(0)
        # cv2.destroyAllWindows()

        start_time = time.time()
        # Dataloader
        bs = 1  # batch_size

        # Run inference
        model.warmup(imgsz=(1 if pt or model.triton else bs, 3, *imgsz))  # warmup
        seen, windows, dt = 0, [], (Profile(), Profile(), Profile())
        if True:
            im0 = image
            im = letterbox(im0, imgsz, stride=stride, auto=pt)[0]  # padded resize
            im = im.transpose((2, 0, 1))[::-1]  # HWC to CHW, BGR to RGB
            im = np.ascontiguousarray(im)  # contiguous
            with dt[0]:
                im = torch.from_numpy(im).to(model.device)
                im = im.half() if model.fp16 else im.float()  # uint8 to fp16/32
                im /= 255  # 0 - 255 to 0.0 - 1.0
                if len(im.shape) == 3:
                    im = im[None]  # expand for batch dim

            # Inference
            with dt[1]:
                visualize = False
                pred = model(im, augment=augment, visualize=visualize)
            # NMS
            pred = non_max_suppression(pred, conf_thres, iou_thres, classes, agnostic_nms, max_det=max_det)

            # Second-stage classifier (optional)
            # pred = utils.general.apply_classifier(pred, classifier_model, im, im0s)

            # Process predictions
            for i, det in enumerate(pred):  # per image
                seen += 1
                #print(det)

                # s += '%gx%g ' % im.shape[2:]  # print string
                gn = torch.tensor(im0.shape)[[1, 0, 1, 0]]  # normalization gain whwh
                if len(det):
                    # Rescale boxes from img_size to im0 size
                    det[:, :4] = scale_boxes(im.shape[2:], det[:, :4], im0.shape).round()

                    # Print results
                    for c in det[:, 5].unique():
                        n = (det[:, 5] == c).sum()  # detections per class
                        # s += f"{n} {names[int(c)]}{'s' * (n > 1)}, "  # add to string
                    # Write results
                    for *xyxy, conf, cls in reversed(det):
                        if (word == names[int(cls)]) or (word == "all"):
                            xywh = (xyxy2xywh(torch.tensor(xyxy).view(1, 4)) / gn).view(-1).tolist()  # normalized xywh
                            # response = {} = get_position(xywh[0],xywh[1])
                            current = [(names[int(cls)])]
                            current = get_position(xywh[0],xywh[1],current)
                            responseArray.append(current)

            # Stream results

        if update:
            strip_optimizer(weights[0])  # update model (to fix SourceChangeWarning)
        if lang == "en":
            response = messageEN(responseArray, word)
        elif lang == "bg":
            response = messageBG(responseArray, word)

        return response


class MyServer(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_body = self.rfile.read(content_length)

        query = urlparse(self.path).query
        query_components = dict(qc.split("=") for qc in query.split("&"))
        word = query_components["word"]
        lang = query_components["lang"]

        nparr = np.fromstring(post_body, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        response = detect(img,word,lang)
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        response = json.dumps(response)
        self.wfile.write(bytes(response ,"utf-8"))
        # self.wfile.write(bytes("<html><head><title>https://pythonbasics.org</title></head>", "utf-8"))

if __name__ == "__main__":        
    webServer = HTTPServer((hostName, serverPort), MyServer)
    print("Server started http://%s:%s" % (hostName, serverPort))

    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped.")
    