package piece;

import main.GamePanel;

public class Pawn extends Piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("w-pawn");
        } else {
            image = getImage("b-pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            //Define Movevalue Based On ColOr Black Or white
            int moveValue ;
            if(color == GamePanel.WHITE){
                moveValue = -1 ;
            }
            else{
                moveValue = 1 ;
            }

            //Check for Hitting Piece
            hittingP = getHittingP(targetCol, targetRow);

            // One Square Movement
            if(targetCol == preCol && targetRow == preRow + moveValue && hittingP == null){
                return  true ;
            }

            //Two Square Movement
            if(targetCol == preCol && targetRow == preRow + moveValue*2 && hittingP == null && moved == false 
            && pieceIsOnStraightLine(targetCol, targetRow)== false){
                return  true ;
            }

            // Diagonal Movement And Capturing the Pieces 
            if(Math.abs(targetCol- preCol) == 1 && targetRow == preRow + moveValue && hittingP != null && hittingP.color != color){
                return  true ;
            }


        }

        return false;
    }

}