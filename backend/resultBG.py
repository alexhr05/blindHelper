from distance import distance

translate = {
    "person":"човек",
    "bicycle":"колело",
    "car":"кола",
    "motorcycle":"мотоциклет",
    "airplane":"самолет",
    "bus":"автобус",
    "train":"влак",
    "truck":"камион",
    "boat":"лодка",
    "traffic light":"светофар",
    "fire hydrant":"пожарен кран",
    "stop sign":"знак стоп",
    "parking meter":"паркинг автомат",
    "bench":"пейка",
    "bird":"птица",
    "cat":"котка",
    "dog":"куче",
    "horse":"кон",
    "sheep":"офца",
    "cow":"крава",
    "elephant":"слон",
    "bear":"мечка",
    "zebra":"зебра",
    "giraffe":"жираф",
    "backpack":"раница",
    "umbrella":"чадър",
    "handbag":"дамска чанта",
    "tie":"вратовръзка",
    "suitcase":"куфар",
    "frisbee":"фризби",
    "skis":"ски",
    "snowboard":"сноуборд",
    "sports ball":"спортна топка",
    "kite":"хвърчило",
    "baseball bat":"бейзболна бухалка",
    "baseball glove":"бейзболна ръкавица",
    "skateboard":"скейтборд",
    "surfboard":"дъска за сърф",
    "tennis racket":"тенис ракета",
    "bottle":"бутилка",
    "wine glass":"чаша за вино",
    "cup":"чаша",
    "fork":"вилица",
    "knife":"нож",
    "spoon":"лъжица",
    "bowl":"купа",
    "banana":"банан",
    "apple":"ябълка",
    "sandwich":"сандвич",
    "orange":"портокал",
    "broccoli":"броколи",
    "carrot":"морков",
    "hot dog":"хот дог",
    "pizza":"пица",
    "donut":"донът",
    "cake":"торта",
    "chair":"стол",
    "couch":"диван",
    "potted plant":"растение в саксия",
    "bed":"легло",
    "dining table":"маса",
    "toilet":"тоалетна",
    "tv":"телевизор",
    "laptop":"лаптоп",
    "mouse":"мишка",
    "remote":"дистанционно",
    "keyboard":"клавиатура",
    "cell phone":"телефон",
    "microwave":"микроволнова",
    "oven":"фурна",
    "toaster":"тостер",
    "sink":"мивка",
    "refrigerator":"хладилник",
    "book":"книга",
    "clock":"часовник",
    "vase":"ваза",
    "scissors":"ножици",
    "teddy bear":"плюшено мече",
    "hair drier":"сешоар",
    "toothbrush":"четка за зъби",
}

def rod(word):
    words = word.split(" ")
    lastChar = words[0][len(words[0])-1]
    if(lastChar == "а") or (lastChar == "я"):
        return 2
    elif(lastChar == "о") or (lastChar == "е"):
        return 3
    else:
        return 1

def num(br, word):
    word = translate[word]
    if br > 2:
        return str(br)
    elif br == 1:
        if rod(word) == 1:
            return "Един"
        elif rod(word) == 2:
            return "Една"
        else:
            return "Едно"
    elif br == 2:
        if rod(word) == 1:
            return "Два"
        elif rod(word) == 2:
            return "Две"
        else:
            return "Две"

plural = {
    "person":"човека",
    "bicycle":"колела",
    "car":"коли",
    "motorcycle":"мотоциклета",
    "airplane":"самолета",
    "bus":"автобуса",
    "train":"влака",
    "truck":"камиона",
    "boat":"лодки",
    "traffic light":"светофара",
    "fire hydrant":"пожарни крана",
    "stop sign":"знака стоп",
    "parking meter":"паркинг автомата",
    "bench":"пейки",
    "bird":"птици",
    "cat":"котки",
    "dog":"кучета",
    "horse":"коня",
    "sheep":"офце",
    "cow":"крави",
    "elephant":"слона",
    "bear":"мечки",
    "zebra":"зебри",
    "giraffe":"жирафа",
    "backpack":"раници",
    "umbrella":"чадъра",
    "handbag":"дамска чанти",
    "tie":"вратовръзки",
    "suitcase":"куфара",
    "frisbee":"фризбита",
    "skis":"ски",
    "snowboard":"сноуборда",
    "sports ball":"спортни топки",
    "kite":"хвърчила",
    "baseball bat":"бейзболни бухалки",
    "baseball glove":"бейзболни ръкавици",
    "skateboard":"скейтборда",
    "surfboard":"дъски за сърф",
    "tennis racket":"тенис ракети",
    "bottle":"бутилки",
    "wine glass":"чаши за вино",
    "cup":"чаши",
    "fork":"вилици",
    "knife":"ножа",
    "spoon":"лъжици",
    "bowl":"купи",
    "banana":"банана",
    "apple":"ябълки",
    "sandwich":"сандвича",
    "orange":"портокала",
    "broccoli":"броколи",
    "carrot":"моркови",
    "hot dog":"хот дога",
    "pizza":"пици",
    "donut":"донъта",
    "cake":"торти",
    "chair":"стола",
    "couch":"дивана",
    "potted plant":"растения в саксия",
    "bed":"легла",
    "dining table":"маси",
    "toilet":"тоалетни",
    "tv":"телевизора",
    "laptop":"лаптопа",
    "mouse":"мишки",
    "remote":"дистанционни",
    "keyboard":"клавиатури",
    "cell phone":"телефона",
    "microwave":"микроволнови",
    "oven":"фурни",
    "toaster":"тостера",
    "sink":"мивки",
    "refrigerator":"хладилника",
    "book":"книги",
    "clock":"часовника",
    "vase":"вази",
    "scissors":"ножици",
    "teddy bear":"плюшени мечета",
    "hair drier":"сешоара",
    "toothbrush":"четки за зъби",
}

def messageBG(arr, word):
    result = "Има "
    objects = []
    if word == "all":
        if len(arr) == 0:
            return "Няма намерени обекти"
        result += str(len(arr)) + " обекта намерени. "
        for det in arr:
            if det[0] in objects:
                continue
            objects.append(det[0])
            brL = count(arr, det[0], "left")
            brM = count(arr, det[0], "mid")
            brR = count(arr, det[0], "right")
            if brL>0:
                result += str(num(brL,det[0])) + " "
                if brL>1:
                    result += plural[det[0]]
                else:
                    result += translate[det[0]]
                result += " вляво, "
            if brM>0:
                result += str(num(brM,det[0])) + " "
                if brM>1:
                    result += plural[det[0]]
                else:
                    result += translate[det[0]]
                result += " по средата, "
            if brR>0:
                result += str(num(brR,det[0])) + " "
                if brR>1:
                    result += plural[det[0]]
                else:
                    result += translate[det[0]]
                result += " вдясно, "
    else:
        if len(arr) == 0:
            return "Няма намерени " + plural[word]
        br = count(arr, arr[0][0])
        if br>1:
            result += str(br) + " " + plural[arr[0][0]] + " пред телефона. "
            for det in arr:
                result += str(num(1,det[0]))+" е " + distance(det[0], det[3], "bg") + " и е "
                result += pos(det)
        else:
            result += str(num(1,arr[0][0])) + " " + translate[arr[0][0]] + " намерен " + distance(arr[0][0], arr[0][3], "bg") + " и "
            result += pos(arr[0])

    return result

def count(arr, word, loc="all"):
    br = 0
    for det in arr:
        if det[0] == word:
            if loc == "left" and det[1]<=1:
                br+=1
            elif loc == "mid" and det[1]==2:
                br += 1
            elif loc == "right" and det[1]>=3:
                br += 1
            elif loc == "all":
                br+=1

    return br

def pos(det):
    result = ""
    if det[1] == 0:
        result += "вляво"
    if det[1] == 1:
        result += "леко вляво"
    if det[1] == 2:
        result += "по средата"
    if det[1] == 3:
        result += "леко вдясно"
    if det[1] == 4:
        result += "вдясно"

    if det[2] == 0:
        result += " и горе. "
    if det[2] == 1:
        result += " и леко нагоре. "
    if det[2] == 2:
        result += ". "
    if det[2] == 3:
        result += " и леко надолу. "
    if det[2] == 4:
        result += " и долу. "
    return result