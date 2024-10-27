import gui
import environment
import robot
import math
#import agent

#agents = [agent.Agent()]

my_robots = []

def draw(pygame, canvas):
    
    mouse_pos = pygame.mouse.get_pos()
    debug_text = gui.font.render(str(mouse_pos[0] / environment.scale) + " " + str(mouse_pos[1] / environment.scale), False, (0,0,0))

    canvas.blit(debug_text, (50, gui.HEIGHT - 50))

    pass

def draw_robots(pygame, canvas):
    my_robots[0].draw(pygame, canvas)
    for my_robot in my_robots:
        my_robot.step()

if __name__ == "__main__":
    print("Running")

    # 80, 938
    for i in range(30):
        my_robots.append(robot.Robot(80, 938, -math.pi + 0.00001))
        my_robots[i].reset()

    environment.setup()
    
    #gui.addDraw(environment.drawObstacles)
    gui.addDraw(draw_robots)
    #gui.addDraw(draw)

    gui.setup()
