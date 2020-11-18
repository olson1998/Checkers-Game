package game;


import gameBox.Game;

public class Main {

    public static void main(String[] args) {
	// write your code here
        game();
        //Board b = new Board();
        //b.checking(40, -7);
    }

    static void game(){
        Game game = new Game();
        int r = 1; boolean win = false;
        while(!win){
            r=game.round(r);
            if(game.isWin()){
                win = true;
                System.out.println("Congrats, the player " + r + " has won the game!");
            }
        }
    }
}
