/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2016
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

   public int dirn;                          // direction of agent
   public int moves = 0;                     // number of moves
   public char prev;                         // previous move
   public char[][] map = new char[100][100]; // constructed map of views from path travelled
   public boolean[][] visited = new boolean[100][100]; // boolean value whether space has been visited
   public List<Point> unvisited = new ArrayList<Point>();
   public int row = 5;                       // number of rows in the map
   public int col = 5;                       // number of columns in the map
   public int r = 2;                         // current row of agent
   public int c = 2;                         // current column of agent
   public int x = 2;                         // start x coordinate of agent
   public int y = 2;                         // start y coordinate of agent
   public boolean found_gold = false;        // true if location of gold has been discovered
   public boolean found_path = false;      // true if path to gold has been found
   public int gx;                            // x coordinate of gold
   public int gy;                            // y coordinate of gold
   public List<Node> path = new ArrayList<Node>();
   public List<Point> axes = new ArrayList<Point>();
   public List<Point> keys = new ArrayList<Point>();
   public List<Point> doors = new ArrayList<Point>();
   public boolean has_axe = false;
   public boolean has_key = false;
   public boolean has_gold = false;
   public boolean map_updated = false;
   private final int COST = 1;

   class Node {
      public Node parent;
      private int nx;
      private int ny;
      public char nch;
      public double f;
      public double g;
      public double h;

      public Node(int x, int y, char ch) {
         nx = x;
         ny = y;
         nch = ch;
      }

      @Override
      public boolean equals(Object other){
         return (this.nx == ((Node) other).nx && this.ny == ((Node)other).ny);
      }

      public void getEstimate(int x, int y) {
         this.h = Math.abs(this.nx-x)+Math.abs(this.ny-y);
      }

   }

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
      queue.add(start);
      boolean found_goal = false;
      Node goal = null;
      while (!queue.isEmpty() && !found_goal) {
         Node current = queue.poll();
         // goal reached and goal obtained
         if (current.nx == to.nx && current.ny == to.ny) {
            found_path = true;
            found_goal = true;
            goal = current;
         }
         // add neighbouring tiles that can be legally moved to
         if (current.ny < col-1 && (map[current.nx][current.ny + 1] == ' ' || map[current.nx][current.ny + 1] == 'g')) {
            Node n = new Node(current.nx, current.ny + 1, map[current.nx][current.ny + 1]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.nx > 0 && (map[current.nx - 1][current.ny] == ' ' || map[current.nx - 1][current.ny] == 'g')) {
            Node n = new Node(current.nx - 1, current.ny, map[current.nx - 1][current.ny]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.ny > 0 && (map[current.nx][current.ny - 1] == ' ' || map[current.nx][current.ny - 1] == 'g')) {
            Node n = new Node(current.nx, current.ny - 1, map[current.nx][current.ny - 1]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
         if (current.nx < row-1 && (map[current.nx + 1][current.ny] == ' ' || map[current.nx + 1][current.ny] == 'g')) {
            Node n = new Node(current.nx + 1, current.ny, map[current.nx + 1][current.ny]);
            n.parent = current;
            n.g = current.g + COST;
            n.getEstimate(to.nx,to.ny);
            n.f = n.g + n.h;
            if(!expanded.contains(n)) {
               queue.add(n);
               expanded.add(n);
            }
         }
      }
      if (found_goal) { // if we have found a path store it
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

   private boolean isVisited(char dir){
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
      }
      return false;
   }

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
         path.remove(next);
         move = 'F';
      } else if ((dirn + 1) % 4 == nextdirn) {
         dirn = (dirn + 1) % 4;
         move = 'L';
      } else {
         dirn = (dirn + 3) % 4;
         move = 'R';
      }
      return move;
   }

   public char explore(char view[][]) {

      char ch;
      char front = view[1][2];
      char left = view[2][1];
      char right = view[2][3];

      // code isn't complete, there are cases that have not been covered yet.

      // if there is a wall in front
      if (front == '~' || front == '*' || (!has_axe && front == 'T') || (!has_key && front == '-')) {
         // and a wall to the left
         if (left == '~' || left == '*' || (!has_axe && left == 'T') || (!has_key && left == '-')) {
            ch = 'R';
            dirn = (dirn + 3) % 4;

            // or a wall to the right
         } else if (right == '~' || right == '*' || (!has_axe && right == 'T') || (!has_key && right == '-')) {
            ch = 'L';
            dirn = (dirn + 1) % 4;
            // or there are nothing on the sides but a wall in front
         } else {
            ch = 'L';
            dirn = (dirn + 1) % 4;
         }
         // if the first left turn we come across is not visited then turn left
      } else if (front == ' ' && !isVisited('F')){
         ch = 'F';

      } else if (left == ' ' && !isVisited('L') && (view[3][1] == '*' || view[3][1] == '~' ||
              (!has_axe && view[3][1] == 'T') || (!has_key && view[3][1] == '-'))) {
         ch = 'L';
         dirn = (dirn + 1) % 4;

      } else if (right == ' ' && isVisited('F')){
         ch = 'R';
         dirn = (dirn + 3) % 4;
         // else you go forward until you find a wall
      } else if (front == ' ') {
         ch = 'F';
      } else if (has_axe && front == 'T') {
         ch = 'C';
      } else if (has_key && front == '-') {
         ch = 'U';
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
      if (prev == 'F' || prev == 'C' || prev == 'U') {
            update_map(view);
      }

      // if agent is now on gold, axe, or key update data
      if (r == gx && c == gy) {
         has_gold = true;
         found_path = false;
      }

      for (Point p : axes) {
         if (r == p.x && c == p.y) {
            has_axe = true;
         }
      }
      for (Point p : keys) {
         if (r == p.x && c == p.y) {
            has_key = true;
         }
      }

      char ch = 'L';
      // agent has the gold
      if (has_gold) {
         if(!found_path){ // if there isn't a path, generate one
            Node start = new Node(x,y,map[x][y]);
            createPathTo(start);
         }
         Node next = path.get(0);
         ch = nextMove(next);

      // agent doesn't have the gold but knows the location of it
      } else if (!has_gold && found_gold) {
         if(!found_path){ // if there isn't a path, try make one
            Node gold = new Node(gx,gy,map[gx][gy]);
            createPathTo(gold);
         }
         if(found_path) { // if the a path was successfully made
            Node next = path.get(0);
            ch = nextMove(next);
         } else { // if you cannot reach gold at the moment
            ch = explore(view);
         }

      // agent is trying to find location of the gold
      } else {
         ch = explore(view);
      }
      moves++;
      print_map();
      prev = ch;
      return ch;
   }

   void create_map(char view[][]) {
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
            }
         }
      } else if (dirn == NORTH) {
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
            }
         }
      } else if (dirn == WEST) {
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
            }
         }
      } else if (dirn == SOUTH) {
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
            }
         }
      }
      map[2][2] = ' ';
      visited[2][2] = true;
      unvisited.remove(new Point(2,2));

   }

   void update_map(char view[][]) {
      if (dirn == EAST) {
         c++;
         if (!visited[r][c]) {
            if (c+2 == col) {                      // if we need to expand map to the right
               for (int i = 0; i < row; i++) {     // add another column
                  map[i][col] = '?';
                  visited[i][col]= false;
                  unvisited.add(new Point(i,col));
               }
               col++;
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r+i][c+2] = view[0][i+2];
               if (map[r+i][c+2] == 'g') {
                  found_gold = true;
                  gx = r+i;
                  gy = c+2;
               }
               if (map[r+i][c+2] == 'a') {
                  axes.add(new Point(r+i,c+2));
               }
               if (map[r+i][c+2] == 'k') {
                  keys.add(new Point(r+i,c+2));
               }
            }
            visited[r][c] = true;
            unvisited.remove(new Point(r,c));
            map_updated = true;
         } else {
            map_updated = false;
         }
      } else if (dirn == NORTH) {
         r--;
         if (!visited[r][c]) {
            if (r-2 < 0) {                         // if we need to expand map up
               for (int i = row; i > 0; i--) {     // add a row by shifting array down a row
                  for (int j = 0; j < col; j++) {
                     map[i][j] = map[i-1][j];
                     visited[i][j] = visited[i-1][j];
                  }
               }
               for (int j = 0; j < col; j++) {     // initialise first row
                  map[0][j] = '?';
                  visited[0][j] = false;
                  unvisited.add(new Point(0,j));
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
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r-2][c+i] = view[0][i+2];
               if (map[r-2][c+i] == 'g') {
                  found_gold = true;
                  gx = r-2;
                  gy = c+i;
               }
               if (map[r-2][c+i] == 'a') {
                  axes.add(new Point(r-2,c+i));
               }
               if (map[r-2][c+i] == 'k') {
                  keys.add(new Point(r-2,c+i));
               }
            }
            visited[r][c] = true;
            unvisited.remove(new Point(r,c));
            map_updated = true;
         } else {
            map_updated = false;
         }
      } else if (dirn == WEST) {
         c--;
         if (!visited[r][c]) {
            if (c-2 < 0) {                         // if we need to expand map to the left
               for (int i = 0; i < row; i++) {     // add a column by shifting array to the right
                  for (int j = col; j > 0; j--) {
                     map[i][j] = map[i][j-1];
                     visited[i][j] = visited[i][j-1];
                  }
               }
               for (int i = 0; i < row; i++) {     // initialise first column
                  map[i][0] = '?';
                  visited[i][0] = false;
                  unvisited.add(new Point(i,0));
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
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r+i][c-2] = view[0][2-i];
               if (map[r+i][c-2] == 'g' && !found_gold) {
                  found_gold = true;
                  gx = r+i;
                  gy = c-2;
               }
               if (map[r+i][c-2] == 'a') {
                  axes.add(new Point(r+i,c-2));
               }
               if (map[r+i][c-2] == 'k') {
                  keys.add(new Point(r+i,c-2));
               }
            }
            visited[r][c] = true;
            unvisited.remove(new Point(r,c));
            map_updated = true;
         } else {
            map_updated = false;
         }
      } else if (dirn == SOUTH) {
         r++;
         if (!visited[r][c]) {
            if (r+2 == row) {                      // if we need to expand map down
               for (int j = 0; j < col; j++) {     // add another row
                  map[row][j] = '?';
                  visited[row][j] = false;
                  unvisited.add(new Point(row,j));
               }
               row++;
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r+2][c+i] = view[0][2-i];
               if (map[r+2][c+i] == 'g' && !found_gold) {
                  found_gold = true;
                  gx = r+2;
                  gy = c+i;
               }
               if (map[r+2][c+i] == 'a') {
                  axes.add(new Point(r+2,c+i));
               }
               if (map[r+2][c+i] == 'k') {
                  keys.add(new Point(r+2,c+i));
               }
            }
            visited[r][c] = true;
            Point p = new Point(r,c);
            unvisited.remove(p);
            map_updated = true;
         } else {
            map_updated = false;
         }
      }
   }

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
            agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
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
