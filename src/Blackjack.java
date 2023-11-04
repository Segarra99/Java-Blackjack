import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Blackjack {

  private class Card {
    String value;
    String type;

    Card(String value, String type) {
      this.value = value;
      this.type = type;
    }

    public String toString() {
      return value + "-" + type;
    }

    public int getValue() {
      if ("AJQK".contains(value)) { //Ace, Jack, Queen or King
        if (value == "A") {
          return 11;
        }
        return 10;
      }
      return Integer.parseInt(value); //2-10
    }

    public boolean isAce() {
      return value == "A";
    }

    public String getImagePath() {
      return "/cards/" + toString() + ".png";
    }
  }

  ArrayList<Card> deck;
  Random random = new Random(); //shuffle deck

  //Dealer
  Card hiddenCard;
  ArrayList<Card> dealerHand;
  int dealerSum;
  int dealerAceCount;

  //Player
  ArrayList<Card> playerHand;
  int playerSum;
  int playerAceCount;

  //Money
  int playerMoney = 10;
  int betMoney = 0;

  //Message
  String message = "";

  //Window
  int boardWidth = 1000;
  int boardHeight = 1000;

  int cardWidth = 160; //Ratio should be 1/1.4
  int cardHeight = 224;

  JFrame frame = new JFrame("Blackjack");
  JPanel gamePanel = new JPanel() {
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);

      g.setFont(new Font("Arial", Font.PLAIN, 30));
      g.setColor(Color.white);
      g.drawString("Dealer's hand:", 50, 60);
      g.drawString("Your hand:", 50, 550);
      g.drawString("Money: " + String.valueOf(playerMoney), 50, 860);
      g.drawString("Bet: " + String.valueOf(betMoney), 50, 910);

      try {
        //Draw hidden card
        Image hiddenCardImg = new ImageIcon(getClass().getResource("/cards/BACK.png")).getImage();
        if (!stayButton.isEnabled()) {
          hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
        }
        g.drawImage(hiddenCardImg, 50, 100, cardWidth, cardHeight, null);

        //Draw dealer's hand
        for (int i = 0; i < dealerHand.size(); i++) {
          Card card = dealerHand.get(i);
          Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
          g.drawImage(cardImg, cardWidth + 55 + (cardWidth + 5)*i, 100, cardWidth, cardHeight, null);
        }

        //Draw player's hand
        for (int i = 0; i < playerHand.size(); i++) {
          Card card = playerHand.get(i);
          Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
          g.drawImage(cardImg, 50 + (cardWidth + 5)*i, 590, cardWidth, cardHeight, null);
        }

        if (!stayButton.isEnabled()) {
          dealerSum = reduceDealerAce();
          playerSum = reducePlayerAce();
          System.out.println("Stay:");
          System.out.println(dealerSum);
          System.out.println(playerSum);

          if (playerSum > 21 && dealerSum > 21) {
            message = "Tie!";
            playerMoney += betMoney;
            betMoney = 0;
            gamePanel.repaint();
          }
          else if (playerSum > 21) {
            message = "You Lose!";
            betMoney = 0;
            gamePanel.repaint();
          }
          else if (dealerSum > 21) {
            message = "You Win!";
            playerMoney += betMoney * 2;
            betMoney = 0;
            gamePanel.repaint();
          }
          //Both you and the dealer both have <= 21
          else if (playerSum == dealerSum) {
            message = "Tie!";
            playerMoney += betMoney;
            betMoney = 0;
            gamePanel.repaint();
          }
          else if (playerSum > dealerSum) {
            message = "You Win!";
            playerMoney += betMoney * 2;
            betMoney = 0;
            gamePanel.repaint();
          }
          else if (playerSum < dealerSum) {
            message = "You Lose!";
            betMoney = 0;
            gamePanel.repaint();
          }

          g.setFont(new Font("Arial", Font.PLAIN, 30));
          g.setColor(Color.white);
          g.drawString(message, 420, 450);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };
  JPanel buttonPanel = new JPanel();
  JButton hitButton = new JButton("Hit");
  JButton stayButton = new JButton("Stay");
  JButton resetButton = new JButton("Reset");
  JButton plusButton = new JButton("+");
  JButton minusButton = new JButton("-");

  Blackjack() {
    startGame();

    frame.setVisible(true);
    frame.setSize(boardWidth, boardHeight);
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    gamePanel.setLayout(new BorderLayout());
    gamePanel.setBackground(new Color(53, 101, 77));
    frame.add(gamePanel);
    
    minusButton.setFocusable(false);
    buttonPanel.add(minusButton);
    plusButton.setFocusable(false);
    buttonPanel.add(plusButton);
    hitButton.setFocusable(false);
    buttonPanel.add(hitButton);
    stayButton.setFocusable(false);
    buttonPanel.add(stayButton);
    resetButton.setFocusable(false);
    buttonPanel.add(resetButton);
    frame.add(buttonPanel, BorderLayout.SOUTH);

    hitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Card card = deck.remove(deck.size()-1);
        playerSum += card.getValue();
        playerAceCount += card.isAce() ? 1 : 0;
        playerHand.add(card);
        if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J
          hitButton.setEnabled(false);
        }
        gamePanel.repaint();
      }
    });

    stayButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        while (dealerSum < playerSum && playerSum <= 21) {
          Card card = deck.remove(deck.size()-1);
          dealerSum += card.getValue();
          dealerAceCount += card.isAce() ? 1 : 0;
          dealerHand.add(card);
          dealerSum = reduceDealerAce();
        }
        gamePanel.repaint();
      }
    });

    resetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        message = "";
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        startGame();
        gamePanel.repaint();
      }
    });

    minusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (betMoney > 0) {
          betMoney -= 1;
          playerMoney += 1;
          gamePanel.repaint();
        }
      }
    });

    plusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (playerMoney > 0) {
          betMoney += 1;
          playerMoney -= 1;
          gamePanel.repaint();
        }
      }
    });

    gamePanel.repaint();
  }

  public void startGame() {
    //Deck
    buildDeck();
    shuffleDeck();

    //Dealer
    dealerHand = new ArrayList<Card>();
    dealerSum = 0;
    dealerAceCount = 0;

    hiddenCard = deck.remove(deck.size()-1); //remove card at last index
    dealerSum += hiddenCard.getValue();
    dealerAceCount += hiddenCard.isAce() ? 1 : 0;

    Card card = deck.remove(deck.size()-1);
    dealerSum += card.getValue();
    dealerAceCount += card.isAce() ? 1 : 0;
    dealerHand.add(card);

    System.out.println("Dealer:");
    System.out.println(hiddenCard);
    System.out.println(dealerHand);
    System.out.println(dealerSum);
    System.out.println(dealerAceCount);

    //Player
    playerHand = new ArrayList<Card>();
    playerSum = 0;
    playerAceCount = 0;

    for (int i = 0; i < 2; i++) {
      card = deck.remove(deck.size()-1);
      playerSum += card.getValue();
      playerAceCount += card.isAce() ? 1 : 0;
      playerHand.add(card);
    }

    System.out.println("Player:");
    System.out.println(playerHand);
    System.out.println(playerSum);
    System.out.println(playerAceCount);
  }

  public void buildDeck() {
    deck = new ArrayList<Card>();
    String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    String[] types = {"C", "D", "H", "S"};

    for (int i = 0; i < types.length; i++) {
      for (int j = 0; j < values.length; j++) {
        Card card = new Card(values[j], types[i]);
        deck.add(card);
      }
    }

    System.out.println("Build Deck:");
    System.out.println(deck);
  }

  public void shuffleDeck() {
    for (int i = 0; i < deck.size(); i++) {
      int j = random.nextInt(deck.size());
      Card currentCard = deck.get(i);
      Card randomCard = deck.get(j);
      deck.set(i, randomCard);
      deck.set(j, currentCard);
    }

    System.out.println("After Shuffle:");
    System.out.println(deck);
  }

  public int reducePlayerAce() {
    while (playerSum > 21 && playerAceCount > 0) {
      playerSum -= 10;
      playerAceCount -= 1;
    }
    return playerSum;
  }

  public int reduceDealerAce() {
    while (dealerSum > 21 && dealerAceCount > 0) {
      dealerSum -= 10;
      dealerAceCount -= 1;
    }
    return dealerSum;
  }
}
