import gui
import environment
import robot
import math
import random
#import agent

#agents = [agent.Agent()]

my_robots = []
generation = 1
step = 1
MAX_STEPS = 500

do_evolution = False

def draw(pygame, canvas):
    
    mouse_pos = pygame.mouse.get_pos()
    #debug_text = gui.font.render(str(mouse_pos[0] / environment.scale) + " " + str(mouse_pos[1] / environment.scale), False, (0,0,0))
    #debug_text = gui.font.render("generation " + str(generation), False, (0,0,0))

    #canvas.blit(debug_text, (50, gui.HEIGHT - 50))

    pass

def draw_robots(pygame=None, canvas=None):
    global generation, step, MAX_STEPS

    top_robot = my_robots[0]

    for my_robot in my_robots:
        if my_robot.getScore() > top_robot.getScore():
            top_robot = my_robot

    if pygame is not None and canvas is not None:
        top_robot.draw(pygame, canvas)

        for a_robot in my_robots:
            if not (a_robot == top_robot):
                pos = a_robot.position

                # draw a dot for the robot
                pygame.draw.circle(canvas, (255, 0, 0), (int(pos[0]) * environment.scale, int(pos[1]) * environment.scale), 3)

    crash_count = 0
    for my_robot in my_robots:
        my_robot.step()
        if (my_robot.inCollision):
            crash_count += 1

    t = 0
    for time in robot.times:
        t += time
    #print("Average time: " + str(t / len(robot.times)) + " -- Total time: " + str(t))
    robot.times = []
    
    #if step % 10 == 0: print("step " + str(step))
    step += 1
    
    if (do_evolution and step > MAX_STEPS) or crash_count == len(my_robots):
        if do_evolution:
            # sort robot by getScore()
            my_robots.sort(key=lambda x: x.getScore(), reverse=True)
        
            # best robot
            best_robot = my_robots[0]
            top_agents = [robot.agent.clone() for robot in my_robots[:math.ceil(len(my_robots) * 0.1)]]  # Top 10%

            print("All robots have crashed. Best robot has score of " + str(best_robot.getScore()))

            # mutation
            for i in range(math.floor(len(my_robots) * 0.1), len(my_robots)):
                # select a random agent from top agents to clone for mutation diversity
                selected_agent = random.choice(top_agents)
                my_robots[i].agent = selected_agent.clone()
                mutation_strength = 1 / (1 + 0.05 * generation) # adaptive mutation strength
                mutation_rate = 0.1
                my_robots[i].agent = my_robots[i].agent.mutate_weights(mutation_strength)

            # slightly mutate mid-range performers to retain some diversity without drastic changes
            for i in range(1, math.floor(len(my_robots) * 0.1)):
                p_thru = i / math.floor(len(my_robots) * 0.1)
                my_robots[i].agent.mutate_weights(0.2 * (1 - p_thru))
        else:
            print("all robots have crashed and/or finished.")

        for i in range(0, len(my_robots)):
            my_robots[i].reset()

        generation += 1
        step = 1
        MAX_STEPS += 50


        print("----GENERATION " + str(generation) + "----")


if __name__ == "__main__":
    print("Running")

    # 80, 938
    for i in range(0):
        my_robots.append(robot.Robot(80, 938, -math.pi + 0.00001))
        my_robots[i].reset()

    my_robots.append(robot.Robot(80, 938, -math.pi + 0.00001, "model419.npy"))

    environment.setup()
    
    gui.addDraw(environment.drawObstacles)
    gui.addDraw(draw_robots)
    gui.addDraw(draw)

    print("STARTING GENERATION 1")

    # while True:
    #     draw_robots()

    gui.setup()
