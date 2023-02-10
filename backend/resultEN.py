def messageEN(arr, word):
    result = "There "
    objects = []
    if word == "all":
        if len(arr) == 0:
            return "No objects found"
        if len(arr)>1:
            result += "are "
            for det in arr:
                if det[0] in objects:
                    continue
                objects.append(det[0])
                br = count(arr, det[0])
                result += str(br) + " "
                if br>1:
                    result += plural(det[0])
                else:
                    result += det[0]
                result += ", "

        else:
            result += "is 1 "
            result += arr[0][0]
    else:
        if len(arr) == 0:
            return "No " + plural(word) + " found"
        br = count(arr, arr[0][0])
        if br>1:
            result += "are " + str(br) + " " + plural(arr[0][0]) + " infront of the phone. "
            for det in arr:
                result += "One "
                if det[1] == 0:
                    result += "is to the left"
                if det[1] == 1:
                    result += "is slightly to the left"
                if det[1] == 2:
                    result += "is in the middle"
                if det[1] == 3:
                    result += "is slightly to the right"
                if det[1] == 4:
                    result += "is to the right"

                if det[2] == 0:
                    result += " and at the top. "
                if det[2] == 1:
                    result += " and slightly up. "
                if det[2] == 2:
                    result += ". "
                if det[2] == 3:
                    result += " and slightly down. "
                if det[2] == 4:
                    result += " and at the bottom. "
        else:
            result += "is one " + arr[0][0] + " found "
            if arr[0][1] == 0:
                result += "to the left"
            if arr[0][1] == 1:
                result += "slightly to the left"
            if arr[0][1] == 2:
                result += "in the middle"
            if arr[0][1] == 3:
                result += "slightly to the right"
            if arr[0][1] == 4:
                result += "to the right"

            if arr[0][2] == 0:
                result += " and at the top."
            if arr[0][2] == 1:
                result += " and slightly up."
            if arr[0][2] == 2:
                result += "."
            if arr[0][2] == 3:
                result += " and slightly down."
            if arr[0][2] == 4:
                result += " and at the bottom."

    return result

def count(arr, word):
    br = 0
    for det in arr:
        if det[0] == word:
            br+=1
    return br

def plural(word):
    if word == "person":
        return "people"
    if word == "bus":
        return "buses"
    if word == "bench":
        return "benches"
    if word == "sheep":
        return "sheep"
    if word == "skis":
        return "pairs of skis"
    if word == "wine glass":
        return "wind glasses"
    if word == "knife":
        return "knives"
    if word == "bench":
        return "benches"
    if word == "sandwich":
        return "sandwiches"
    if word == "couch":
        return "couches"
    if word == "scissors":
        return "scissors"
    if word == "toothbrush":
        return "toothbrushes"
    return word + "s"