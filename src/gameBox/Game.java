package gameBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Game {
    private final Board board;
    private final Scanner scanner;

    public Game() {
        this.board = new Board();
        this.scanner = new Scanner(System.in);
    }

    public int round (int player){
        separator();
        int enemy = 2; //number of enemy's team
        int ppl; //player's pawn location
        boolean roundEnd = false;
        if(player == 2){enemy = 1; }
        System.out.println("Round of player " + player);
        displayBoard(player);
        while(!roundEnd){
            boolean canCapture = isEnemyToCapture(player);
            if(!canCapture) { ppl = pawnMoves(player); }
            else{ ppl = pawnCaptures(player);}
            roundEnd = true;
            queenPromotion(player, ppl);
        }
        return enemy;
    }

    private int pawnSelecting(int player, boolean isMoving){
        List<Integer> content; String msg;
        if(isMoving){ content = thosePawnsCanMove(player); msg = "Pawns on those coordinates can move: ";} //list of player's pawns that are able to move
        else{content = thosePawnsCanCapture(player); msg = "Those pawns can capture enemy's pawn: ";} // list of pawn's location that are able to capture enemy's pawn
        displayThosePawnsCan(pawnsCoordinates(content), msg);
        int sp = select("Select pawn: "); //selected pawn's coordinates
        sp = checkingInput(sp,1 ,content, "Select pawn: ");
        return content.get(sp-1);
    }

    private int pawnMoves(int player){
        boolean moved = false; int ppl= 0;
        while (!moved) {
            ppl = pawnSelecting(player, true); // selected pawn's location
            List<Integer> pd = possibleDirectionsToMove(player, ppl);//possible directions that selected pawn is able to move on
            int ind = board.pawnIndexFinder(findTeam(player), ppl);// pawn's index on location ppl
            displayPossibleDirections(pd, player);
            int sd = select("Select direction: ");
            //sd = checkingInput(sd,0 , pd, "Select direction: ");
            if (sd == 0) { pawnMoves(player);}
            if(sd != 0){
                int d = pd.get(sd - 1);
                if(findTeam(player)[ind].getType()==0){ MOVING(player, ppl, d);}
                else if(findTeam(player)[ind].getType()==1) {
                    List<Integer> pnl = board.possibleNewLocations(ppl, pd.get(sd - 1));
                    pnl.remove(0);
                    List<String> nc = new ArrayList<>();
                    for (int l : pnl) {
                        nc.add(intToCoordinates(l));
                    }
                    displayThosePawnsCan(nc, "Those pawns can be moved to: ");
                    System.out.println("0) undo");
                    int nl = select("Select new location: ");
                    MOVING(player, ppl, d, pnl.get(nl - 1));
                }
            }
            ppl = findTeam(player)[ind].getLocation();
            moved = true;
        }
        return ppl;
    }

    private int pawnCaptures(int player){
        int ppl = pawnSelecting(player, false);
        while(canPawnCapture(player, ppl)) {
            List<Integer> pd = possibleDirectionsToCapture(player, ppl);
            displayPossibleDirections(pd, player);
            int sd = select("Select direction: ");
            //sd = checkingInput(sd,0 ,pd, "Select direction: ");
            if(sd != 0) {
                int ind = board.pawnIndexFinder(findTeam(player), ppl);
                int epl = 0;
                int d = pd.get(sd - 1);
                if (findTeam(player)[ind].getType() == 0) {
                    epl = enemysLocation(ppl, d);
                    CAPTURING(player, ppl, epl);
                }
                else if (findTeam(player)[ind].getType() == 1) {
                    List<Integer> pl = board.possibleNewLocations(ppl, d);
                    epl = enemysLocation(player, pl, d);
                    List<Integer> pnl = board.possibleNewLocations(epl + d, d);//possible new locations after capturing
                    List<String> pnc = pawnsCoordinates(pnl);
                    displayThosePawnsCan(pnc, "Those pawns can be moved to: ");
                    int nl = select("Select new location: ");
                    //checkingInput(nl,1 , pnl, "Select new location: ");
                    CAPTURING(player, ppl, pnl.get(nl-1), epl);
                }
                ppl = findTeam(player)[ind].getLocation();
                if (canPawnCapture(player, ppl)) { displayBoard(player);}
            }
            else{pawnCaptures(player);}
        }
        return ppl;
    }


    public boolean isWin(){
        boolean yes = false;
        for(Pawn pawn : board.getTeam1()){
            yes = true;
            if(pawn.getType() != -1){ yes = false; break;}
        }
        if(!yes){
            for(Pawn pawn : board.getTeam2()){
                yes = true;
                if(pawn.getType() != -1){ yes = false; break; }
            }
        }
        return yes;
    }

    private Pawn[] findTeam(int t) {
        Pawn[] team = board.getTeam1();
        if (t == 2) {
            team = board.getTeam2();
        }
        return team;
    }

    private void MOVING(int player, int pl, int d){
        // pl -> pawn location; d -> direction
        board.moving(findTeam(player), pl, d);
    }

    private void MOVING(int player, int pl, int d, int nl){
        int i = board.pawnIndexFinder(findTeam(player), pl); int q = 1;
        if(findTeam(player)[i].getType() == 1) {
            int dis = nl - pl;
            q = dis / d;
        }
        board.moving(findTeam(player), pl, d, q);

    }

    private void CAPTURING(int player, int ppl, int epl){
        //ppl -> player's pawn location; epl -> enemy's pawn location
        int enemy = 2;
        if(player == 2){enemy =1; }
        board.capturing(findTeam(player), findTeam(enemy), ppl, epl);
    }

    private void CAPTURING(int player, int ppl, int npl ,int epl){
        //ppl -> player's pawn location; epl -> enemy's pawn location
        int enemy = 2;
        if(player == 2){enemy =1; }
        board.capturing(findTeam(player), findTeam(enemy), ppl, npl , epl);
    }

    private List<Integer> thosePawnsCanMove(int t) {
        List<Integer> coordinates = new ArrayList<>();
        boolean can;
        int m1 = -9; int m2 = -7;
        if (t == 2) {
            m1 = m1 * -1;
            m2 = m2 * -1;
        }
        for (Pawn pawn : findTeam(t)) {
            can = false;
            if (pawn.getType() != -1) {
                int l = pawn.getLocation();
                if (board.canMove(findTeam(t), t, l, m1)) {
                    can = true;
                } else if (board.canMove(findTeam(t), t, l, m2)) {
                    can = true;
                }
                if (pawn.getType() == 1) {
                    if (board.canMove(findTeam(t), t, l, m1 * -1)) {
                        can = true;
                    } else if (board.canMove(findTeam(t), t, l, m2 * -1)) {
                        can = true;
                    }
                }
                if (can) {
                    coordinates.add(l);
                }
            }
        }
        return coordinates;
        }


    private List<Integer> possibleDirectionsToMove(int team, int pl){
        List<Integer> directions = new ArrayList<>(); int i = board.pawnIndexFinder(findTeam(team), pl);
        int m1 = -9; int m2=-7;
        if(team ==2) { m1 = m1*-1; m2 = m2*-1;}
        if(board.canMove(findTeam(team), team , pl, m1)){ directions.add(m1);}
        if(board.canMove(findTeam(team), team,  pl, m2)){ directions.add(m2);}
        if(findTeam(team)[i].getType() == 1){
            if(board.canMove(findTeam(team), team , pl, m1*-1)){ directions.add(m1*-1);}
            if(board.canMove(findTeam(team), team,  pl, m2*-1)){ directions.add(m2*-1);}
        }
        return directions;
    }

    private List<Integer> thosePawnsCanCapture(int player){
        List<Integer> coordinates = new ArrayList<>();
        for(int i =0; i < findTeam(player).length; i++){
                if(canPawnCapture(player, findTeam(player)[i].getLocation()) && findTeam(player)[i].getType() !=-1){
                    coordinates.add(findTeam(player)[i].getLocation());
            }
        }
        return coordinates;
    }

    private boolean isEnemyToCapture(int player){
        int enemy = 2; boolean can = false;
        if(player == 2){ enemy = 1;}
        for(Pawn pp : findTeam(player)){
            for(Pawn ep : findTeam(enemy)){
                if((board.canCapture(findTeam(player), findTeam(enemy) ,pp.getLocation(), ep.getLocation())) && pp.getType() != -1 && ep.getType()!= -1){ can = true; break;}
            }
        }
        return can;
    }

    private void queenPromotion(int team, int pl){
        int[] prL = new int[4]; int ind = board.pawnIndexFinder(findTeam(team), pl);
        if(team == 1){ prL = new int[]{1, 3, 5, 7};}
        if(team == 2){ prL = new int[]{56,58,60,62};}
        for(int l : prL){
            if(findTeam(team)[ind].getLocation() == l && findTeam(team)[ind].getType() ==0){
                board.promoting(findTeam(team), pl);
                break;
            }
        }
    }

    private boolean canPawnCapture(int player, int ppl){
        boolean yes = false; int enemy = 2;
        if(player == 2){enemy =1;}
        for(Pawn pawn : findTeam(enemy)){
            if(board.canCapture(findTeam(player), findTeam(enemy) ,ppl, pawn.getLocation())){
                yes = true;
                break;
            }
        }
        return yes;
    }

    private List<Integer> possibleDirectionsToCapture(int player, int ppl) {
        int enemy = 2;
        if(player == 2){ enemy = 1; }
        List<Integer> directions = new ArrayList<>();
        int ind = board.pawnIndexFinder(findTeam(player), ppl);
        int[] pm = {-9, -7, 7, 9};
        for(int m : pm){
                if ((findTeam(player)[ind].getType() == 0) && board.canCapture(findTeam(player), findTeam(enemy), ppl,ppl +m) && isPawn(findTeam(enemy), ppl+m)) {
                    directions.add(m);
                }
                else if (findTeam(player)[ind].getType() == 1) {
                    List<Integer> pnl = board.possibleNewLocations(ppl, m);
                    Integer el = enemysLocation(player, pnl, m);
                    if ((el != null) && board.canCapture(findTeam(player), findTeam(enemy), ppl, el) && isPawn(findTeam(enemy), el)) {
                        directions.add(m);
                    }
                }
        }
        return directions;
    }



    private boolean isPawn(Pawn[] team, int l){
        boolean yes = true; int i = 0;
        for(Pawn pawn: team){
            if(pawn.getLocation()== l){break;}
            i++;
        }
        if(i >= team.length){ yes = false;}
        return yes;
    }


    private int enemysLocation(int ppl, int d){
        //returing enemy's pawn locations which will be capture on direction "d"
        return ppl + d;
    }

    private Integer enemysLocation(int player, List<Integer> nl, int m){
        //System.out.println("nl: " + nl);
        //System.out.println("m= " + m);
        Integer epl = null; int enemy = 2;
        if (player == 2) {
            enemy = 1;
        }
        if(nl.size()!=0) {
            for (Pawn pawn : findTeam(enemy)) {
                for (int l : nl) {
                    int pl = l + m;
                    if (pl == pawn.getLocation()) {
                        epl = pl;
                        break;
                    }
                }
                if (epl != null) {
                    break;
                }
            }
        }
        return epl;
    }

    private void separator(){
        System.out.println("----------------------------------------------------");
    }



    private void displayBoard(int team){
        String [] board = boardBuilder(team); int l =9;
        if(team == 2) { l = 0; }
        separator(); if(team == 1){System.out.println("    A    B    C    D    E    F    G    H");}
        else{System.out.println("    H    G    F    E    D    C    B    A");}
        int i =0;
        for(String line : board) {
            if (team == 1) { --l;}
            else { ++l;}
            System.out.println(l + " " + board[i] + " " + l); i++;
        }
        if(team == 1){System.out.println("    A    B    C    D    E    F    G    H");
        }
        else{System.out.println("    H    G    F    E    D    C    B    A");
        }
        separator();
    }

    private String [] boardBuilder(int team) {
        String[] board = new String[8];
        int s = 7;
        int ind = 0;
        StringBuilder sb = new StringBuilder();
        if (team == 1){
            for (int i = 0; i < 64; i++) {
                sb.append(field(i));
                if (i == s) {
                    board[ind] = sb.toString(); ind++;
                    s = s + 8;
                    sb = new StringBuilder();
                }
            }
        }
        else {
            s=63-7;
            for (int i = 63; i >= 0; i--) {
                sb.append(field(i));
                if (i == s) {
                    board[ind] = sb.toString(); ind++;
                    s = s - 8;
                    sb = new StringBuilder();
                }
            }
        }
        return board;
    }

    private String field(int i){
        StringBuilder sb = new StringBuilder();
        Integer match = null;
        for (Pawn pawn : board.getTeam1()) {
            if (i == pawn.getLocation()) {
                String f = " ";
                match = pawn.getLocation();
                if (pawn.getType() == 0) { f = "[ " + pawn.getColor() + " ]"; }
                else if(pawn.getType() == 1){ f = "[ " + pawn.getColor() + "*]";}
                sb.append(f);
            }
        }
        if (match == null) {
            for (Pawn pawn : board.getTeam2()) {
                if (i == pawn.getLocation()) {
                    match = pawn.getLocation();
                    String f = " ";
                    if (pawn.getType() == 0) { f = "[ " + pawn.getColor() + " ]"; }
                    else if(pawn.getType() == 1){ f = "[ " + pawn.getColor() + "*]";}
                    sb.append(f);
                }
            }
        }
        if (match == null) {
            int lvl = (i - i%8)/8; char c = 1;
            if((lvl%2 == 0 && i %2 == 1) || (lvl%2==1 && i % 2 ==0)){sb.append("[   ]");}
            else{ sb.append("[").append(c).append(c).append(c).append("]");}
        }
        return sb.toString();
    }

    private int checkingInput(int s, int start, List <Integer> list, String msg){
        if((s < start || s > list.size())) {
            System.out.println("Selected action has not been found...");
            s = select(msg);
            s = checkingInput(s, start, list, "*"+msg);
        }
        return s;
    }

    private int select(String msg){
        System.out.print(msg);
        return scanner.nextInt();
    }


    private void displayThosePawnsCan(List<String> cm, String msg){
        System.out.println(msg + cm);
        for (int i = 0; i < cm.size(); i++) {
            System.out.println(i + 1 + ") coordinate: " + cm.get(i));
        }
    }

    private void displayPossibleDirections(List<Integer> pd, int t){
       String dir = " "; int d;
        for (int i = 0; i < pd.size(); i++) {
            d = pd.get(i);
            if(t == 2){ d = d*-1;}
            if(d == -9){dir="forward left"; }
            if(d == -7){dir="forward right"; }
            if(d == 7){dir="backward left"; }
            if(d == 9){dir="backward right"; }
            System.out.println(i + 1 + ") " + dir);
        }
        System.out.println("0) undo");
    }

    private List<String> pawnsCoordinates(List <Integer> pc){
        List<String> coordinates = new ArrayList<>();
        for(int c : pc){ coordinates.add(intToCoordinates(c));}
        return coordinates;
    }

    private String intToCoordinates(int l){
        StringBuilder sb = new StringBuilder();
        int n = -1*(l-63-(l%8))/8; n=n+1;
        sb.append((char) (65 + l%8)); sb.append(n);
        return sb.toString();
    }


}
