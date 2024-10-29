import pygame
import threading

canvas = None

listeners = []

keys = {
    "LEFT": False,
    "RIGHT": False,
    "UP": False,
    "DOWN": False,
    "Q": False,
    "E": False
}

WIDTH = 600
HEIGHT = 600

font = None

def setup():
    global canvas

    pygame.init()

    font = pygame.font.Font('freesansbold.ttf', 18)
    
    canvas = pygame.display.set_mode((WIDTH, HEIGHT))

    pygame.display.set_caption("Training")

    done = False

    clock = pygame.time.Clock()

    while not done:
        #clock.tick(60)

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done = True

            # this could be made so much more efficient but its fine for now
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_LEFT:
                    keys["LEFT"] = True
                if event.key == pygame.K_RIGHT:
                    keys["RIGHT"] = True
                if event.key == pygame.K_UP:
                    keys["UP"] = True
                if event.key == pygame.K_DOWN:
                    keys["DOWN"] = True
                if event.key == pygame.K_e:
                    keys["E"] = True
                if event.key == pygame.K_q:
                    keys["Q"] = True
            if event.type == pygame.KEYUP:
                if event.key == pygame.K_LEFT:
                    keys["LEFT"] = False
                if event.key == pygame.K_RIGHT:
                    keys["RIGHT"] = False
                if event.key == pygame.K_UP:
                    keys["UP"] = False
                if event.key == pygame.K_DOWN:
                    keys["DOWN"] = False
                if event.key == pygame.K_e:
                    keys["E"] = False
                if event.key == pygame.K_q:
                    keys["Q"] = False

        draw()
        pygame.display.update()

def draw():
    pygame.draw.rect(canvas, (255,255,255), (0,0,WIDTH,HEIGHT))
    for listener in listeners:
        listener(pygame, canvas)
    pygame.display.flip()
    pass

def addDraw(listener):
    listeners.append(listener)
    print("Added Listener")


def set_interval(func, sec):
    def func_wrapper():
        set_interval(func, sec)
        func()
    t = threading.Timer(sec, func_wrapper)
    t.start()
    return t

if __name__ == "__main__":
    #setup()
    pass