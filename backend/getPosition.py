def get_position(x, y, r1):

    if x < 0.2:
        x_pos = 0
    elif x < 0.4:
        x_pos = 1
    elif x < 0.6:
        x_pos = 2
    elif x < 0.8:
        x_pos = 3
    else:
        x_pos = 4

    r1.append(x_pos)

    if y < 0.2:
        y_pos = 0
    elif y < 0.4:
        y_pos = 1
    elif y < 0.6:
        y_pos = 2
    elif y < 0.8:
        y_pos = 3
    else:
        y_pos = 4

    r1.append(y_pos)

    return r1

def get_center(*xywh):
    print(*xywh)
    x, y, w, h = xywh
    center_x = x + w/2
    center_y = y + h/2
    return (center_x, center_y)