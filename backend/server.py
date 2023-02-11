import json
import urllib
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

hostName = "localhost"
serverPort = 80
general = {
    "пожарни кран": "fire hydrant",
    "знака стоп": "stop sign",
    "дамски чант": "handbag",
    "спортни топк": "sports ball",
    "бейзболни бухалк": "baseball bat",
    "бейзболни ръкавиц": "baseball glove",
    "дъски за сърф": "surfboard",
    "чаши за вино": "wine glass",
    "растения в сакс": "potted plant",
    "маси за хранене": "dining table",
    "плюшени мече": "teddy bear",
    "четки за зъби": "toothbrush",

    "човек": "person",
    "хора": "person",

    "колел": "bicycle",
    "велосипед": "bicycle",

    "автомобил": "car",
    "кол": "car",

    "мотоциклет": "motorcycle",
    "мотор": "motorcycle",

    "самолет": "airplane",
    "автобус": "bus",
    "влак": "train",
    "камион": "truck",

    "лодк": "boat",
    "кораб": "boat",

    "светофар": "traffic light",

    "пожарен хидрант": "fire hydrant",
    "пожарен кран": "fire hydrant",

    "знак за спиране": "stop sign",
    "знак стоп": "stop sign",

    "паркинг метър": "parking meter",
    "пейк": "bench",
    "птиц": "bird",
    "котк": "cat",
    "куче": "dog",
    "кон": "horse",
    "овц": "sheep",
    "крав": "cow",
    "слон": "elephant",
    "мечк": "bear",
    "зебр": "zebra",
    "жираф": "giraffe",
    "раниц": "backpack",
    "чадър": "umbrella",

    "чант": "handbag",
    "торб": "handbag",

    "вратовръзк": "tie",
    "куфар": "suitcase",
    "фризби": "frisbee",
    "ски": "skis",
    "сноуборд": "snowboard",
    "спортна топка": "sports ball",
    "хвърчил": "kite",
    "бейзболна бухалка": "baseball bat",
    "бейзболна ръкавица": "baseball glove",
    "скейтборд": "skateboard",

    "дъска за сърф": "surfboard",
    "сърф дъск": "surfboard",

    "ракета за тенис": "tennis racket",
    "тенис ракет": "tennis racket",

    "бутилк": "bottle",
    "шише": "bottle",

    "чаша за вино": "wine glass",
    "чаш": "cup",
    "вилиц": "fork",
    "нож": "knife",
    "лъжиц": "spoon",
    "куп": "bowl",
    "банан": "banana",
    "ябълк": "apple",
    "сандвич": "sandwich",
    "портокал": "orange",
    "броколи": "broccoli",
    "морков": "carrot",
    "хот-дог": "hot dog",
    "хот дог": "hot dog",
    "пиц": "pizza",

    "поничк": "donut",
    "донът": "donut",

    "торт": "cake",
    "стол": "chair",
    "диван": "couch",
    "растение в саксия": "potted plant",

    "легл": "bed",
    "креват": "bed",

    "маса за хранене": "dining table",
    "тоалетн": "toilet",

    "телевизор": "tv",
    "монитор": "tv",

    "лаптоп": "laptop",
    "мишк": "mouse",

    "дистанционно на телевизор": "remote",
    "клавиатур": "keyboard",

    "мобилен телефон": "cell phone",
    "телефон": "cell phone",

    "микровълнова печк": "microwave",
    "фурн": "oven",
    "тостер": "toaster",
    "мивк": "sink",
    "хладилник": "refrigerator",
    "книг": "book",
    "часовник": "clock",
    "ваз": "vase",
    "ножиц": "scissors",
    "плюшено мече": "teddy bear",
    "сешоар за коса": "hair drier",
    "четка за зъби": "toothbrush",

    "person":"person",
    "human":"person",

    "bicycle": "bicycle",
    "bike": "bicycle",

    "car": "car",
    "automobile": "car",
    "vehicle": "car",

    "motorcycle": "motorcycle",
    "motorbike": "motorcycle",

    "plane": "airplane",
    "aircraft": "airplane",

    "bus": "bus",
    "train": "train",
    "locomotive": "train",

    "truck": "truck",
    "lorry": "truck",

    "boat": "boat",
    "ship": "boat",

    "traffic light": "traffic light",
    "hydrant": "fire hydrant",
    "stop sign": "stop sign",
    "parking meter": "parking meter",
    "bench": "bench",
    "bird": "bird",
    "cat": "cat",
    "dog": "dog",
    "horse": "horse",

    "sheep": "sheep",
    "lamb": "sheep",

    "cow": "cow",
    "elephant": "elephant",
    "bear": "bear",
    "zebra": "zebra",
    "giraffe": "giraffe",
    "backpack": "backpack",
    "umbrella": "umbrella",
    "handbag": "handbag",
    "tie": "tie",

    "suitcase": "suitcase",
    "luggage": "suitcase",

    "frisbee": "frisbee",
    "flying disc": "frisbee",

    "skis": "skis",
    "snowboard": "snowboard",
    "ball": "sports ball",
    "kite": "kite",
    "baseball bat": "baseball bat",
    "baseball glove": "baseball glove",
    "skateboard": "skateboard",
    "surfboard": "surfboard",
    "racket":"tennis racket",
    "bottle": "bottle",
    "wine glass": "wine glass",

    "cup": "cup",
    "glass": "cup",

    "fork": "fork",
    "knife": "knife",
    "spoon": "spoon",
    "bowl": "bowl",
    "banana": "banana",
    "apple": "apple",
    "sandwich": "sandwich",
    "orange": "orange",
    "broccoli": "broccoli",
    "carrot": "carrot",
    "hot dog": "hot dog",
    "pizza": "pizza",

    "donut": "donut",
    "doughnut": "donut",

    "cake": "cake",

    "chair": "chair",
    "seat": "chair",

    "couch": "couch",
    "plant": "potted plant",
    "bed": "bed",
    "dining table": "dining table",
    "toilet": "toilet",

    "tv": "tv",
    "television": "tv",

    "laptop": "laptop",
    "mouse": "mouse",
    "remote": "remote",
    "keyboard": "keyboard",
    "cell phone": "cell phone",
    "microwave": "microwave",
    "oven": "oven",
    "toaster": "toaster",
    "sink": "sink",

    "refrigerator": "refrigerator",
    "fridge": "refrigerator",

    "book": "book",
    "clock": "clock",
    "vase": "vase",

    "scissors": "scissors",
    "shears": "scissors",

    "teddy bear": "teddy bear",

    "hair drier": "hair drier",
    "blow dryer": "hair drier",
    "hair blower": "hair drier",

    "toothbrush": "toothbrush"
}

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
        word = urllib.parse.unquote(word)
        if any(key in word for key in general):
            key = next((key for key in general if key in word), None)
            if key:
                word = general[key]

        nparr = np.fromstring(post_body, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        response = detect(img,word,lang)
        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.end_headers()
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

