/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2016
 */

import java.util.*;
import java.io.*;
import java.net.*;

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
   public int row = 5;                       // number of rows in the map
   public int col = 5;                       // number of columns in the map
   public int r = 2;                         // current row of agent
   public int c = 2;                         // current column of agent
   public int x = 2;                         // start x coordinate of agent
   public int y = 2;                         // start y coordinate of agent
   public boolean found_gold = false;
   public int gx;
   public int gy;
   public boolean has_axe = false;
   public boolean has_key = false;
   public boolean has_gold = false;



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
      char ch;
      int n;
      Random ran = new Random();
      if (view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T' || view[1][2]== '-') {
         n = ran.nextInt(2);
         if (n == 0) {
            ch = 'L';
            dirn = (dirn+1) % 4;
         } else {
            ch = 'R';
            dirn = (dirn+3) % 4;
         }
      } else {
         n = ran.nextInt(4);
         if (n == 0) {
            ch = 'L';
            dirn = (dirn+1) % 4;
         } else if (n == 1) {
            ch = 'R';
            dirn = (dirn+3) % 4;
         } else {
            ch = 'F';
         }
      }
      moves++;
      print_map();
      prev = ch;
      return ch;

      // REPLACE THIS CODE WITH AI TO CHOOSE ACTION!

/*      int ch=0;

      print_view(view);

      System.out.print("Enter Action(s): ");

      try {
         while ( ch != -1 ) {
            // read character from keyboard
            ch  = System.in.read();

            switch( ch ) { // if character is a valid action, return it
               case 'F': case 'L': case 'R': case 'C': case 'U':
               case 'f': case 'l': case 'r': case 'c': case 'u':
                  return((char) ch );
            }
         }
      }
      catch (IOException e) {
         System.out.println ("IO error:" + e );
      }

      return 0;*/
   }

   void create_map(char view[][]) {
      if (dirn == EAST) {
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[4-j][i];
               visited[i][j] = false;
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
            }
         }
      } else if (dirn == NORTH) {
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[i][j];
               visited[i][j] = false;
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
            }
         }
      } else if (dirn == WEST) {
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[j][4-i];
               visited[i][j] = false;
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
            }
         }
      } else if (dirn == SOUTH) {
         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
               map[i][j] = view[4-i][4-j];
               visited[i][j] = false;
               if (map[i][j] == 'g') {
                  found_gold = true;
                  gx = i;
                  gy = j;
               }
            }
         }
      }
      map[2][2] = 'x';
      visited[2][2] = true;
   }

   void update_map(char view[][]) {
      if (dirn == EAST) {
         c++;
         if (!visited[r][c]) {
            if (c+2 == col) {                      // if we need to expand map to the right
               for (int i = 0; i < row; i++) {     // add another column
                  map[i][col] = '?';
                  visited[i][col]= false;
               }
               col++;
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r+i][c+2] = view[0][i+2];
               if (map[r+i][c+2] == 'g' && !found_gold) {
                  found_gold = true;
                  gx = r+i;
                  gy = c+2;
               }
            }
            visited[r][c] = true;
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
               }
               row++;
               r++;
               x++;
               if (found_gold) {
                  gx++;
               }
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r-2][c+i] = view[0][i+2];
               if (map[r-2][c+i] == 'g' && !found_gold) {
                  found_gold = true;
                  gx = r-2;
                  gy = c+i;
               }
            }
            visited[r][c] = true;
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
               }
               col++;
               c++;
               y++;
               if (found_gold) {
                  gy++;
               }
            }
            for (int i = -2; i <= 2; i++) {        // update map with current view
               map[r+i][c-2] = view[0][2-i];
               if (map[r+i][c-2] == 'g' && !found_gold) {
                  found_gold = true;
                  gx = r+i;
                  gy = c-2;
               }
            }
            visited[r][c] = true;
         }
      } else if (dirn == SOUTH) {
         r++;
         if (!visited[r][c]) {
            if (r+2 == row) {                      // if we need to expand map down
               for (int j = 0; j < col; j++) {     // add another row
                  map[row][j] = '?';
                  visited[row][j] = false;
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
            }
            visited[r][c] = true;
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
