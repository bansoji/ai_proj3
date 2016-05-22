/*********************************************
 *  Agent.java
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2016
 *
 *
 *  Description :
 *
 *  Our agent initially uses a common maze solving method of moving along the left boundary and spiralling towards the
 *  center to discover unvisited spaces. Every time the view of the agent contains new spaces, a 2D array representing a
 *  learned map is updated using a single orientation. The location of the gold and any tools are also stored upon
 *  discovery in lists of points. If the location of the gold is known an A* search using a Manhattan distance heuristic
 *  is performed to find a path. If a path cannot be found and the location of tools are known then an A* search finds a
 *  path to a tool, otherwise the agent moves using the initial approach. If the agent has the gold an A* search is
 *  performed to find a path back to the start.
 *
 *  Stepping stones is dealt with via the A* where each node in the path contains the amount of stones held and as the
 *  A* searches over water for the shortest path to the gold, the stone count is decremented resulting in the shortest
 *  path to the gold with the least amount of stones used to attain that shortest path.
 *
 *  Authors :
 *
 *  Banson Tong   z3460406
 *  Aaron Oni     z3459482
 *
 */

import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.List;

public class Agent {

   final static int EAST   = 0;
   final static int NORTH  = 1;
   final static int WEST   = 2;
   final static int SOUTH  = 3;

   private int dirn;                                        // direction of agent
   private int moves = 0;                                   // number of moves
   private char prev;                                       // previous action
   private char[][] map = new char[80][80];                 // constructed map of views from path travelled
   private boolean[][] visited = new boolean[80][80];       // boolean value whether space has been visited
   private List<Point> unvisited = new ArrayList<Point>();  // list of unvisited points
   private int row = 5;                                     // number of rows in the map
   private int col = 5;                                     // number of columns in the map
   private int r = 2;                                       // current row of agent
   private int c = 2;                                       // current column of agent
   private int x = 2;                                       // start x coordinate of agent
   private int y = 2;                                       // start y coordinate of agent
   private boolean found_gold = false;                      // true if location of gold has been discovered
   private boolean found_path = false;                      // true if path to goal has been found
   private int gx;                                          // x coordinate of gold
   private int gy;                                          // y coordinate of gold
   private List<Node> path = new ArrayList<Node>();         // path created using A* search
   private List<Point> axes = new ArrayList<Point>();       // list of axe locations
   private List<Point> keys = new ArrayList<Point>();       // list of key locations
   private List<Point> stones = new ArrayList<Point>();     // list of stepping stone locations
   private boolean has_axe = false;                         // true if the agent has collected an axe
   private boolean has_key = false;                         // true if the agent has collected a key
   private int has_stones = 0;                              // number of stones the agent has collected
   private boolean has_gold = false;                        // true if the agent has the gold
   private boolean turning = false;
   final static int COST = 1;

   /**
    * Local Node class for A* search that contains the heuristic, x & y coords and the stones left
    */
   class Node {
      private Node parent;
      private int nx;             // x coord
      private int ny;             // y coord
      private char nch;
      private double f;
      private double g;
      private double h;           // heuristic
      private int stones_left;

      public Node(int x, int y, char ch) {
         nx = x;
         ny = y;
         nch = ch;
         stones_left = 0;
      }

      @Override
      public boolean equals(Object other){
         return (this.nx == ((Node) other).nx && this.ny == ((Node)other).ny);
      }

      public void getEstimate(int x, int y) {
         this.h = Math.abs(this.nx-x)+Math.abs(this.ny-y);
      }

   }

   /**
    * A* search using Manhattan distance heuristic
    * @param to destination
     */
   private void createPathTo(Node to){
      PriorityQueue<Node> queue = new PriorityQueue<Node>(new Comparator<Node>() {
         @Override
         public int compare(Node o1, Node o2) {
            if (o1.f > o2.f) {
               return 1;
            } else if (o1.f < o2.f) {
               return -1;
            } else {
               return 0;
            }
         }
      });
      ArrayList<Node> expanded = new ArrayList<Node>();
      Node start = new Node(r, c, map[r][c]);
      start.parent = null;
      start.g = 0;
      start.getEstimate(to.nx,to.ny);
      start.f = start.g + start.h;
      start.stones_left = has_stones;
      queue.add(start);
      boolean found_goal = false;
      Node goal = null;
      while (!queue.isEmpty() && !found_goal) {
         Node current = queue.poll();
         // goal reached
         if (current.nx == to.nx && current.ny == to.ny) {
            found_path = true;
            found_goal = true;
            goal = current;
         }
         // add neighbouring tiles that can be moved to
         if (current.ny < col-1 && (map[current.nx][current.ny+1] == ' ' || map[current.nx][current.ny+1] == 'g' ||
                 (has_axe && map[current.nx][current.ny+1] == 'T') || (has_key && map[current.nx][current.ny+1] == '-')
                 || map[current.nx][current.ny+1] == 'a' || map[current.nx][current.ny+1] == 'k'
                 || map[current.nx][current.ny+1] == 'o' || map[current.nx][current.ny+1] == 'O'
                 || (current.stones_left != 0 && map[current.nx][current.ny+1] == '~'))) {
            Node n = new Node(current.nx, current.ny + 1, map[current.nx][current.ny + 1]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            if(current.stones_left > 0 && map[current.nx][current.ny + 1] == '~') {
               n.stones_left = current.stones_left - 1;
            } else {
               n.stones_left = current.stones_left;
            }
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.nx > 0 && (map[current.nx-1][current.ny] == ' ' || map[current.nx-1][current.ny] == 'g' ||
                 (has_axe && map[current.nx-1][current.ny] == 'T') || (has_key && map[current.nx-1][current.ny] == '-')
                 || map[current.nx-1][current.ny] == 'a' || map[current.nx-1][current.ny] == 'k'
                 || map[current.nx-1][current.ny] == 'o' || map[current.nx-1][current.ny] == 'O'
                 ||(current.stones_left != 0 && map[current.nx-1][current.ny] == '~'))) {
            Node n = new Node(current.nx - 1, current.ny, map[current.nx - 1][current.ny]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            if(current.stones_left > 0 && map[current.nx - 1][current.ny] == '~') {
               n.stones_left = current.stones_left - 1;
            } else {
               n.stones_left = current.stones_left;
            }
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.ny > 0 && (map[current.nx][current.ny-1] == ' ' || map[current.nx][current.ny-1] == 'g' ||
                 (has_axe && map[current.nx][current.ny-1] == 'T') || (has_key && map[current.nx][current.ny-1] == '-')
                 || map[current.nx][current.ny-1] == 'a' || map[current.nx][current.ny-1] == 'k'
                 || map[current.nx][current.ny-1] == 'o' || map[current.nx][current.ny-1] == 'O'
                 || (current.stones_left != 0 && map[current.nx][current.ny-1] == '~'))) {
            Node n = new Node(current.nx, current.ny - 1, map[current.nx][current.ny - 1]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            if(current.stones_left > 0 && map[current.nx][current.ny - 1] == '~') {
               n.stones_left = current.stones_left - 1;
            } else {
               n.stones_left = current.stones_left;
            }
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.nx < row-1 && (map[current.nx+1][current.ny] == ' ' || map[current.nx+1][current.ny] == 'g'  ||
                 (has_axe && map[current.nx+1][current.ny] == 'T') || (has_key && map[current.nx+1][current.ny] == '-')
                 || map[current.nx+1][current.ny] == 'a' || map[current.nx+1][current.ny] == 'k'
                 || map[current.nx+1][current.ny] == 'o' || map[current.nx+1][current.ny] == 'O'
                 || (current.stones_left != 0 && map[current.nx+1][current.ny] == '~'))) {
            Node n = new Node(current.nx + 1, current.ny, map[current.nx + 1][current.ny]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            if(current.stones_left > 0 && map[current.nx + 1][current.ny] == '~') {
               n.stones_left = current.stones_left - 1;
            } else {
               n.stones_left = current.stones_left;
            }
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
      }
      // store the path if found
      if (found_goal) {
         List<Node> reverse = new ArrayList<Node>();
         for (Node node = goal; node != start; node = node.parent) {
            reverse.add(node);
         }
         path.clear();
         for (int i = 0; i < reverse.size(); i++) {
            path.add(i, reverse.get(reverse.size() - i - 1));
         }

      }
   }

    /**
     * Method that returns if the cell in the direction of the agent is visited
     * @param dir direction relative to agent
     * @return boolean of whether cell has been visited
     */
   private boolean isVisited(char dir){
      // if left of agent
      if (dir == 'L') {
         if (dirn == NORTH) {
            return visited[r][c - 1];
         } else if (dirn == EAST) {
            return visited[r - 1][c];
         } else if (dirn == SOUTH) {
            return visited[r][c + 1];
         } else if (dirn == WEST) {
            return visited[r + 1][c];
         }
         // if front of agent
      } else if (dir == 'F'){
         if (dirn == NORTH) {
            return visited[r-1][c];
         } else if (dirn == EAST) {
            return visited[r][c+1];
         } else if (dirn == SOUTH) {
            return visited[r+1][c];
         } else if (dirn == WEST) {
            return visited[r][c-1];
         }
         // if right of agent
      } else if (dir == 'R'){
         if (dirn == NORTH) {
            return visited[r][c+1];
         } else if (dirn == EAST) {
            return visited[r+1][c];
         } else if (dirn == SOUTH) {
            return visited[r][c-1];
         } else if (dirn == WEST) {
            return visited[r-1][c];
         }
      }
      return false;
   }

   /**
    * Determines the next action of the agent to reach the next state/node in path
    * @param next next given Node from path to reach
    * @return the command or action to reach that path
     */
   private char nextMove(Node next){
      int nextdirn = 0;
      char move;
      if (next.nx == r && next.ny == c + 1) {
         nextdirn = EAST;
      } else if (next.nx == r - 1 && next.ny == c) {
         nextdirn = NORTH;
      } else if (next.nx == r && next.ny == c - 1) {
         nextdirn = WEST;
      } else if (next.nx == r + 1 && next.ny == c) {
         nextdirn = SOUTH;
      }
      if (dirn == nextdirn) {

         if (next.nch == 'T') {
            move = 'C';
            next.nch = ' ';
         } else if (next.nch == '-') {
            move = 'U';
            next.nch = ' ';
         } else {
            move = 'F';
            path.remove(next);
         }
      } else if ((dirn + 1) % 4 == nextdirn) {
         dirn = (dirn + 1) % 4;
         move = 'L';
      } else {
         dirn = (dirn + 3) % 4;
         move = 'R';
      }
      return move;
   }

   /**
    * Traversal algorithm to cover boundaries and then moving towards centre areas
    * @param view given 5x5 view of agent
    * @return next command/action that should be taken
     */
   public char simpleMove(char view[][]) {

      char ch;
      char front = view[1][2];
      char left = view[2][1];
      char right = view[2][3];

      if (has_axe && front == 'T') {               // if you have axe, chop the tree in front
         ch = 'C';
      } else if (has_axe && left == 'T') {         // else if it is to either side, turn towards it
         ch = 'L';
         dirn = (dirn + 1) % 4;
      } else if (has_axe && right == 'T'){
         ch = 'R';
         dirn = (dirn + 3) % 4;
      } else if (has_key && front == '-') {        // if you have key, open the door in front
         ch = 'U';
      } else if (has_key && left == '-') {         // else turn towards the door
         ch = 'L';
         dirn = (dirn + 1) % 4;
      } else if (has_key && right == '-'){
         ch = 'R';
         dirn = (dirn + 3) % 4;

         // if there is a wall in front
      } else if (front == '~' || front == '*' || (!has_axe && front == 'T') || (!has_key && front == '-')) {
         // and a wall to the left
         if (left == '~' || left == '*' || (!has_axe && left == 'T') || (!has_key && left == '-')) {
            ch = 'R';
            dirn = (dirn + 3) % 4;
            turning = true;

            // or a wall to the right
         } else if (right == '~' || right == '*' || (!has_axe && right == 'T') || (!has_key && right == '-')) {
            ch = 'L';
            dirn = (dirn + 1) % 4;
            turning = true;
            // or there are nothing on the sides but a wall in front
         } else {
            if((isVisited('L') && isVisited('R')) || (!isVisited('L') && !isVisited('R'))){
               Random ran = new Random();
               if (ran.nextInt(2) == 0){
                  ch = 'R';
                  dirn = (dirn + 3) % 4;
               } else {
                  ch = 'L';
                  dirn = (dirn + 1) % 4;
               }
            } else if(isVisited('L')){
               ch = 'R';
               dirn = (dirn + 3) % 4;
            } else {
               ch = 'L';
               dirn = (dirn + 1) % 4;
            }
            turning = true;

         }
         // if the front is not visited, keep going forwards
      } else if ((front == ' ' || front == 'a' || front == 'k') && !isVisited('F')){
         ch = 'F';
         turning = false;
         // turn left if the left is open and not visited or if both the front and the left is visited
      } else if ((left == ' ' || left == 'a' || left == 'k') && (!isVisited('L') || (isVisited('F') && isVisited('L'))) && (view[3][1] == '*' || view[3][1] == '~' ||
              (!has_axe && view[3][1] == 'T') || (!has_key && view[3][1] == '-')) && !turning) {
         ch = 'L';
         dirn = (dirn + 1) % 4;
         turning = true;
         // if the right hasn't been traversed, traverse it
      } else if (front == ' ' && isVisited('F') && right == ' ' && !isVisited('R')){
         ch = 'R';
         dirn = (dirn + 3) % 4;
         turning = true;
         // go forward until you find a wall
      } else if (front == ' ') {
         ch = 'F';
         turning = false;

      } else {
         Random ran = new Random();
         int rn = ran.nextInt(2);
         if (rn == 0) {
            ch = 'L';
            dirn = (dirn + 1) % 4;
         } else {
            ch = 'R';
            dirn = (dirn + 3) % 4;
         }
      }
      return ch;
   }

   /**
    * Agent looks for tools and traverses to them via A* or to unvisited areas of map using simpleMove()
    * @param view given 5x5 view of agent
    * @return next action
     */
   public char explore(char view[][]) {
      char ch;
      // if axe location is found, try and find a path
      if (!has_axe && axes.size() > 0) {
         for (Point p : axes) {
            Node axe = new Node(p.x, p.y, map[p.x][p.y]);
            createPathTo(axe);
            if (found_path) {
               break;   // if a path is found, break from loop
            }
         }
      }
      // if key location is known try to find a path
      if (!has_key && keys.size() > 0 && !found_path) {
         for (Point p : keys) {
            Node key = new Node(p.x,p.y,map[p.x][p.y]);
            createPathTo(key);
            if (found_path) {
               break;
            }
         }
      }
      // if stones location is known
      if (!stones.isEmpty() && !found_path){
         for (Point p : stones){
            Node stone = new Node(p.x,p.y,map[p.x][p.y]);
            createPathTo(stone);
            if (found_path) {
               break;
            }
         }
      }
      // if a path is found to either of the above
      if (found_path) {
         Node next = path.get(0);
         ch = nextMove(next);         // decide next action using path generated from A*
      } else {
         ch = simpleMove(view);       // traverse
      }
      return ch;
   }

    /**
     * Returns the final action determined from the algorithm and A*
     * @param view the 5x5 view of agent
     * @return the final action that will be carried out
     */
   public char get_action( char view[][] ) {

      if (moves == 0) {
         switch (view[2][2]) {
            case '^':
               dirn = NORTH;
               break;
            case '>':
               dirn = EAST;
               break;
            case 'v':
               dirn = SOUTH;
               break;
            case '<':
               dirn = WEST;
               break;
         }
         create_map(view);
      }
      update_map(view);

      // if agent is now on gold
      if (r == gx && c == gy) {
         has_gold = true;
         found_path = false;
      }

      // if agent is on axe when the agent does not own one, pick it up
      if(!has_axe) {
         for (Point p : axes) {
            if (r == p.x && c == p.y) {
               has_axe = true;
               found_path = false;
            }
         }
      }

      // if the agent is on the key when they do not own one, pick it up
      if(!has_key) {
         for (Point p : keys) {
            if (r == p.x && c == p.y) {
               has_key = true;
               found_path = false;
            }
         }
      }

      // if the agent is on a stone, pick it up
      if(!stones.isEmpty()) {
         Point toDelete = null;
         for (Point p : stones) {
            if (r == p.x && c == p.y) {
               has_stones++;
               toDelete = p;
               found_path = false;
            }
         }
         if(toDelete != null){
            stones.remove(toDelete);
         }
      }

      // decrease stone count when traversing over water
      if(prev == 'F' && map[r][c] == '~') has_stones--;
      char ch;

      // if the agent has the gold it will move back to the starting position
      if (has_gold) {
         if(!found_path){ // generate a path back to the start
            Node start = new Node(x,y,map[x][y]);
            createPathTo(start);
         }
         Node next = path.get(0);
         ch = nextMove(next);

      // agent does not have the gold but knows its location
      } else if (!has_gold && found_gold) {
         if(!found_path){ // try to find a path to the gold
            Node gold = new Node(gx,gy,map[gx][gy]);
            createPathTo(gold);
         }
         if(found_path) { // if the gold can be reached move along path
            Node next = path.get(0);
            ch = nextMove(next);
         } else { // if gold cannot be reached try and find tools or unvisited areas
            ch = explore(view);
         }

      // agent is trying to find location of the gold
      } else {
         if(found_path) { // if a path to an axe or key was found move along it
            Node next = path.get(0);
            ch = nextMove(next);
         } else { // try and find an axe or key or move to unvisited areas
            ch = explore(view);
         }
      }
      moves++;
      //print_map();
      prev = ch;
      return ch;
   }

   /**
    * Construct a "memory" or map of the seen areas from view
    * @param view given 5x5 view of agent
     */
   void create_map(char view[][]) {

      // if agent is facing east, update map relatively
      if (dirn == EAST) {
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[4-j][i];
               visited[i][j] = false;
               unvisited.add(new Point(i,j));
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
               if (map[i][j] == 'a') {
                  axes.add(new Point(i,j));
               }
               if (map[i][j] == 'k') {
                  keys.add(new Point(i,j));
               }
               if (map[i][j] == 'o') {
                  stones.add(new Point(i,j));
               }
            }
         }
      } else if (dirn == NORTH) {   // if agent is facing north
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[i][j];
               visited[i][j] = false;
               unvisited.add(new Point(i,j));
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
               if (map[i][j] == 'a') {
                  axes.add(new Point(i,j));
               }
               if (map[i][j] == 'k') {
                  keys.add(new Point(i,j));
               }
               if (map[i][j] == 'o') {
                  stones.add(new Point(i,j));
               }
            }
         }
      } else if (dirn == WEST) { // if agent is facing west
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[j][4-i];
               visited[i][j] = false;
               unvisited.add(new Point(i,j));
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
               if (map[i][j] == 'a') {
                  axes.add(new Point(i,j));
               }
               if (map[i][j] == 'k') {
                  keys.add(new Point(i,j));
               }
               if (map[i][j] == 'o') {
                  stones.add(new Point(i,j));
               }
            }
         }
      } else if (dirn == SOUTH) {   // if agent is facing south
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[4-i][4-j];
               visited[i][j] = false;
               unvisited.add(new Point(i,j));
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
               if (map[i][j] == 'a') {
                  axes.add(new Point(i,j));
               }
               if (map[i][j] == 'k') {
                  keys.add(new Point(i,j));
               }
               if (map[i][j] == 'o') {
                  stones.add(new Point(i,j));
               }
            }
         }
      }
      map[2][2] = ' ';
      visited[2][2] = true;
      unvisited.remove(new Point(2,2));

   }

   /**
    * Updates the memory map generated from create_map()
    * @param view given 5x5 view around agent
     */
   void update_map(char view[][]) {
      map[r][c] = ' ';

      if (prev == 'F') {
         if (dirn == EAST) {
            c++;
            if (!visited[r][c]) {
               if (c + 2 == col) {                       // if we need to expand map to the right
                  for (int i = 0; i < row; i++) {        // add another column
                     map[i][col] = '?';
                     visited[i][col] = false;
                     unvisited.add(new Point(i, col));
                  }
                  col++;
               }
               for (int i = -2; i <= 2; i++) {           // update map with current view
                  map[r + i][c + 2] = view[0][i + 2];
                  if (map[r + i][c + 2] == 'g') {
                     found_gold = true;
                     gx = r + i;
                     gy = c + 2;
                  }
                  if (map[r + i][c + 2] == 'a') {
                     Point p = new Point(r + i, c + 2);
                     if (!axes.contains(p)) {
                        axes.add(p);
                     }
                  }
                  if (map[r + i][c + 2] == 'k') {
                     Point p = new Point(r + i, c + 2);
                     if (!keys.contains(p)) {
                        keys.add(p);
                     }
                  }
                  if (map[r + i][c + 2] == 'o') {
                     Point p = new Point(r + i, c + 2);
                     if (!stones.contains(p)) {
                        stones.add(p);
                     }
                  }
               }
               visited[r][c] = true;
               unvisited.remove(new Point(r, c));
            }
         } else if (dirn == NORTH) {
            r--;
            if (!visited[r][c]) {
               if (r - 2 < 0) {                          // if we need to expand map up
                  for (int i = row; i > 0; i--) {        // add a row by shifting array down a row
                     for (int j = 0; j < col; j++) {
                        map[i][j] = map[i - 1][j];
                        visited[i][j] = visited[i - 1][j];
                     }
                  }
                  for (int j = 0; j < col; j++) {        // initialise first row
                     map[0][j] = '?';
                     visited[0][j] = false;
                     unvisited.add(new Point(0, j));
                  }
                  row++;
                  r++;
                  x++;
                  if (found_gold) {
                     gx++;
                  }
                  for (Point p : axes) {
                     p.x++;
                  }
                  for (Point p : keys) {
                     p.x++;
                  }
                  for (Point p : stones) {
                     p.x++;
                  }
                  if (path.size() > 0) {
                     for (Node pnode : path) {
                        pnode.nx++;
                     }
                  }
               }
               for (int i = -2; i <= 2; i++) {           // update map with current view
                  map[r - 2][c + i] = view[0][i + 2];
                  if (map[r - 2][c + i] == 'g') {
                     found_gold = true;
                     gx = r - 2;
                     gy = c + i;
                  }
                  if (map[r - 2][c + i] == 'a') {
                     Point p = new Point(r - 2, c + i);
                     if (!axes.contains(p)) {
                        axes.add(p);
                     }
                  }
                  if (map[r - 2][c + i] == 'k') {
                     Point p = new Point(r - 2, c + i);
                     if (!keys.contains(p)) {
                        keys.add(p);
                     }
                  }
                  if (map[r - 2][c + i] == 'o') {
                     Point p = new Point(r - 2, c + i);
                     if (!stones.contains(p)) {
                        stones.add(p);
                     }
                  }
               }
               visited[r][c] = true;
               unvisited.remove(new Point(r, c));
            }
         } else if (dirn == WEST) {
            c--;
            if (!visited[r][c]) {
               if (c - 2 < 0) {                          // if we need to expand map to the left
                  for (int i = 0; i < row; i++) {        // add a column by shifting array to the right
                     for (int j = col; j > 0; j--) {
                        map[i][j] = map[i][j - 1];
                        visited[i][j] = visited[i][j - 1];
                     }
                  }
                  for (int i = 0; i < row; i++) {        // initialise first column
                     map[i][0] = '?';
                     visited[i][0] = false;
                     unvisited.add(new Point(i, 0));
                  }
                  col++;
                  c++;
                  y++;
                  if (found_gold) {
                     gy++;
                  }
                  for (Point p : axes) {
                     p.y++;
                  }
                  for (Point p : keys) {
                     p.y++;
                  }
                  for (Point p : stones) {
                     p.y++;
                  }
                  if (path.size() > 0) {
                     for (Node pnode : path) {
                        pnode.ny++;
                     }
                  }
               }
               for (int i = -2; i <= 2; i++) {           // update map with current view
                  map[r + i][c - 2] = view[0][2 - i];
                  if (map[r + i][c - 2] == 'g' && !found_gold) {
                     found_gold = true;
                     gx = r + i;
                     gy = c - 2;
                  }
                  if (map[r + i][c - 2] == 'a') {
                     Point p = new Point(r + i, c - 2);
                     if (!axes.contains(p)) {
                        axes.add(p);
                     }
                  }
                  if (map[r + i][c - 2] == 'k') {
                     Point p = new Point(r + i, c - 2);
                     if (!keys.contains(p)) {
                        keys.add(p);
                     }
                  }
                  if (map[r + i][c - 2] == 'o') {
                     Point p = new Point(r + i, c - 2);
                     if (!stones.contains(p)) {
                        stones.add(p);
                     }
                  }
               }
               visited[r][c] = true;
               unvisited.remove(new Point(r, c));
            }
         } else if (dirn == SOUTH) {
            r++;
            if (!visited[r][c]) {
               if (r + 2 == row) {                       // if we need to expand map down
                  for (int j = 0; j < col; j++) {        // add another row
                     map[row][j] = '?';
                     visited[row][j] = false;
                     unvisited.add(new Point(row, j));
                  }
                  row++;
               }
               for (int i = -2; i <= 2; i++) {           // update map with current view
                  map[r + 2][c + i] = view[0][2 - i];
                  if (map[r + 2][c + i] == 'g' && !found_gold) {
                     found_gold = true;
                     gx = r + 2;
                     gy = c + i;
                  }
                  if (map[r + 2][c + i] == 'a') {
                     Point p = new Point(r + 2, c + i);
                     if (!axes.contains(p)) {
                        axes.add(p);
                     }
                  }
                  if (map[r + 2][c + i] == 'k') {
                     Point p = new Point(r + 2, c + i);
                     if (!keys.contains(p)) {
                        keys.add(p);
                     }
                  }
                  if (map[r + 2][c + i] == 'o') {
                     Point p = new Point(r + 2, c + i);
                     if (!stones.contains(p)) {
                        stones.add(p);
                     }
                  }
               }
               visited[r][c] = true;
               unvisited.remove(new Point(r, c));
            }
         }
      } else if (prev == 'C' || prev == 'U') {
         if (dirn == EAST) {
            map[r][c+1] = ' ';
         } else if (dirn == NORTH) {
            map[r-1][c] = ' ';
         } else if (dirn == WEST) {
            map[r][c-1] = ' ';
         } else if (dirn == SOUTH) {
            map[r+1][c] = ' ';
         }
      }
   }

   /**
    * Method to print the memory map for debugging purposes
    */
   void print_map() {
      int i,j;
      System.out.println();
      System.out.println("MAP");
      for( i=0; i < row; i++ ) {
         for( j=0; j < col; j++ ) {
               if (i == r && j == c) {
                  switch( dirn ) {
                     case NORTH:
                        System.out.print('^');
                        break;
                     case SOUTH:
                        System.out.print('v');
                        break;
                     case EAST:
                        System.out.print('>');
                        break;
                     case WEST:
                        System.out.print('<');
                        break;
                  }
               } else {
                  System.out.print(map[i][j]);
               }
         }
         System.out.println();
      }
      System.out.println();
   }

   void print_view( char view[][] )
   {
      int i,j;

      System.out.println("\n+-----+");
      for( i=0; i < 5; i++ ) {
         System.out.print("|");
         for( j=0; j < 5; j++ ) {
            if(( i == 2 )&&( j == 2 )) {
               System.out.print('^');
            }
            else {
               System.out.print( view[i][j] );
            }
         }
         System.out.println("|");
      }
      System.out.println("+-----+");
   }

   public static void main( String[] args )
   {
      InputStream in  = null;
      OutputStream out= null;
      Socket socket   = null;
      Agent  agent    = new Agent();
      char   view[][] = new char[5][5];
      char   action   = 'F';
      int port;
      int ch;
      int i,j;

      if( args.length < 2 ) {
         System.out.println("Usage: java Agent -p <port>\n");
         System.exit(-1);
      }

      port = Integer.parseInt( args[1] );

      try { // open socket to Game Engine
         socket = new Socket( "localhost", port );
         in  = socket.getInputStream();
         out = socket.getOutputStream();
      }
      catch( IOException e ) {
         System.out.println("Could not bind to port: "+port);
         System.exit(-1);
      }

      try { // scan 5-by-5 window around current location
         while( true ) {
            for( i=0; i < 5; i++ ) {
               for( j=0; j < 5; j++ ) {
                  if( !(( i == 2 )&&( j == 2 ))) {
                     ch = in.read();
                     if( ch == -1 ) {
                        System.exit(-1);
                     }
                     view[i][j] = (char) ch;
                  }
               }
            }
            //agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
            action = agent.get_action( view );
            out.write( action );
         }
      }
      catch( IOException e ) {
         System.out.println("Lost connection to port: "+ port );
         System.exit(-1);
      }
      finally {
         try {
            socket.close();
         }
         catch( IOException e ) {}
      }
   }
}
