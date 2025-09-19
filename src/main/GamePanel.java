package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;
import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 640;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

// PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    Piece activeP;
    public static Piece castlingP;

//COLOR 
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

//BOOLEANS
    public boolean canMove;
    public boolean validSquare;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces() {
        // White team
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        // Black team
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        for (int i = 0; i < source.size(); i++) {
            target.add(source.get(i));

        }
    }

    @Override
    public void run() {

        //GAME LOOP 
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        ///MOUSE BUTTON IS PRESSED ///
        if (mouse.pressed == true) {
            if (activeP == null) {
                //if active piece s null  check if you can move piece
                for (Piece piece : simPieces) {
                    // if mouse is on ally Piece , pick it up as activeP
                    if (piece.color == currentColor
                            && piece.col == mouse.x / Board.SQUARE_SIZE
                            && piece.row == mouse.y / Board.SQUARE_SIZE) {
                        activeP = piece;
                    }
                }
            } else {
                // if Player is holding a piece , simulate the move 
                simulate();
            }
        }

        //  Bug Solved 
        ///MOUSED IS PRESSED ///
        if (mouse.pressed == false) {

            if (activeP != null) {
                if (validSquare) {

                    //MOVE CONFIRMED
                    //Update the piece list in a case piece is captured and removed during simulation 
                    copyPieces(simPieces, pieces);
                    activeP.updatePosition();

                    if (castlingP != null) {
                        castlingP.updatePosition();
                    }

                    
                    changePlayer();
                } else {
                    // move is not valid so reset everything 
                    copyPieces(pieces, simPieces);

                    activeP.resetPosition();
                    activeP = null;
                }

            }
        }

    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        //reset the piece list in every loop 
        //this is basically for restoring the removed  pieces  during simulation 
        copyPieces(pieces, simPieces);

        //RESET CASTLING PIECES POSITION 
        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        //if piece is being held update , its postion 
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        // check if Active is hovering over reachable square 
        if (activeP.canMove(activeP.col, activeP.row)) {

            canMove = true;

            // if hitting a piece remove it from a list 
            if (activeP.hittingP != null) {
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            validSquare = true;
        }
    }

    public void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    public void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
        } else {
            currentColor = WHITE;
        }
        activeP = null;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        //BOARD
        board.draw(g2);

        //PIECE
        for (Piece p : simPieces) {
            p.draw(g2);
        }

        if (activeP != null) {
            if (canMove) {
                g2.setColor(Color.gray);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            //draw active piece at end so it wont be hidden 
            activeP.draw(g2);
        }

        // DISPLAY STATUS MESSAGES
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antique", Font.PLAIN, 28));
        g2.setColor(Color.yellow);

        if (currentColor == WHITE) {
            g2.drawString("White's Turn", 720, 600);
        } else {
            g2.drawString("Black's Turn", 720, 100); // keep same Y to align
        }

    }
}
