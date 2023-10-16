import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class GoBoom extends Cards {

	public GoBoom() {
		super();
	}

	public void initialScore() {
		// scores intialize to 0
		scores = new HashMap<>();
		int numOfPlayers = 4;
		for (int i = 0; i < numOfPlayers; i++) {
			scores.put(i, 0);
		}
	}

	public void resetGame() {
		initialScore();
		currentTrickIndex = 0;
		currentPlayerIndex = 0;

		cardDeck();
		playersCards.clear();
		centerCards.clear();

		playersCard();

		setCenterCards();

		System.out.println("\nGame reset. All scores are set to zero. Round and trick number restart from 1.");
		System.out.println("Playing the game...");
	}

	public void playerTurn() {
		char turn = centerCards.get(0).charAt(1);
		int playerTurn = 1;
		switch (turn) {
		case 'A':
		case '5':
		case '9':
		case 'K':
			playerTurn = 1;
			break;
		case '2':
		case '6':
		case 'X':
			playerTurn = 2;
			break;
		case '3':
		case '7':
		case 'J':
			playerTurn = 3;
			break;
		case '4':
		case '8':
		case 'Q':
			playerTurn = 4;
			break;
		default:
			System.out.println("Unknown turn value");
		}
		currentPlayerIndex = playerTurn - 1;
	}

	public void gameStatus() {
		System.out.println("\nTrick #" + (currentTrickIndex + 1)); // set the number of round
		for (int i = 0; i < playersCards.size(); i++) {
			System.out.println("Player " + (i + 1) + " Cards: " + playersCards.get(i));
		}
		System.out.println("Center Cards  : " + centerCards);
		System.out.println("Deck          : " + cardDeck);
		System.out.print("Scores        : ");
		for (int i = 0; i < scores.size(); i++) {
			System.out.print("Player " + (i + 1) + " = " + scores.get(i) + " | ");
		}
		System.out.println();
	}

	private void scoreUpdated() {
		// get currentPlayerIndex from score ArrayList and set the score of the currentPlayerIndex
		scores.put(currentPlayerIndex, scores.getOrDefault(currentPlayerIndex, 0) + 1);
	}

	public void playTurn() throws IOException {
		LinkedList<String> currentPlayerCards = playersCards.get(currentPlayerIndex);

		System.out.println("\nPlayer " + (currentPlayerIndex + 1) + "'s turn.");

		// check if the deck is empty and player's card is not playable, player will be skipped
		if (cardDeck.isEmpty() && !isCardPlayable(currentPlayerCards)) {
			System.out.println("The Deck is empty and Player " + (currentPlayerIndex + 1)
					+ "'s cards cannot be played. Skip to next player...");
			currentPlayerIndex = (currentPlayerIndex + 1) % playersCards.size(); // Move to the next player
			return;
		}

		System.out.print("Enter a card to play (or 'draw' to draw a card or 'more' to see more options): ");
		Scanner userInput = new Scanner(System.in);
		String input = userInput.nextLine().toUpperCase();

		while (!input.equals("DRAW") && !currentPlayerCards.contains(input) && !input.equals("MORE")) {
			System.out.print("Invalid card! Enter a valid card or 'draw' or 'more': ");
			input = userInput.nextLine().toUpperCase();
		}

		if (input.equals("DRAW")) {
			// Draw cards until a valid card can be played
			String drawnCard = drawCardFromDeck();
			currentPlayerCards.add(drawnCard);
			System.out.println("Player " + (currentPlayerIndex + 1) + " drew a card: " + drawnCard);

			while (!isValidPlay(drawnCard)) {
				System.out.println("Drawn card cannot be played. Drawing another card...");
				drawnCard = drawCardFromDeck();
				currentPlayerCards.add(drawnCard);
				System.out.println("Player " + (currentPlayerIndex + 1) + " drew a card: " + drawnCard);
			}

			// Play the valid drawn card
			currentPlayerCards.remove(drawnCard);
			centerCards.add(drawnCard);
			System.out.println("Player " + (currentPlayerIndex + 1) + " played " + drawnCard);
		}

		else if (input.equals("MORE")) {
			while (true) {
				System.out.println("\nMORE OPTIONS");
				System.out.println("S - Quit the game after saving");
				System.out.println("Q - Quit the game without saving");
				System.out.println("R - Reset the game");
				System.out.println("B - Back");
				System.out.print("\nEnter command: ");
				input = userInput.nextLine().toUpperCase();

				if (input.equals("S")) {
					saveGame();
					playGame(); // return to main menu after saving
				}

				else if (input.equals("Q")) {
					System.out.print("Quitting the game without saving...\n");
					playGame(); // return to main menu without saving
				}

				else if (input.equals("R")) {
					// go back to main menu, doonno yet can ask sir
					resetGame();
					startGame();
				}

				else if (input.equals("B")) {
					return;
				}

				else {
					System.out.println("Invalid command! Please choose one of the above!");
				}
			}
		}

		else {
			// Check if the entered card is valid
			while (!isValidPlay(input)) {
				System.out.print("Invalid card! Enter a valid card or 'draw': ");
				input = userInput.nextLine().toUpperCase(); // continue to prompt the user for valid input until a valid
															// card or command is entered, preventing an infinite loop
			}
			// Play the valid input card
			currentPlayerCards.remove(input);
			centerCards.add(input);
			System.out.println("Player " + (currentPlayerIndex + 1) + " played " + input);
		}

		currentPlayerIndex = (currentPlayerIndex + 1) % playersCards.size(); // Move to the next player

		int cardCenterAccepted = 4;

		if (currentTrickIndex == 0) {
			cardCenterAccepted++; // to ensure center accept 5 card in center for trick 1 only
		}

		if (centerCards.size() == cardCenterAccepted) {
			// End of the round, determine the winner of the trick
			String winningCard = centerCards.get(0);
			String winningSuit = String.valueOf(winningCard.charAt(0));
			int winningRankIndex = getRankIndex(String.valueOf(winningCard.charAt(1)));

			for (String card : centerCards) {
				String suit = String.valueOf(card.charAt(0));
				int rankIndex = getRankIndex(String.valueOf(card.charAt(1)));

				if (suit.equals(winningSuit) && rankIndex < winningRankIndex) {
					winningCard = card;
					winningSuit = suit;
					winningRankIndex = rankIndex;
				}
			}

			int winnerIndex = centerCards.indexOf(winningCard);

			if (currentTrickIndex == 0){
				currentPlayerIndex = (currentPlayerIndex + (winnerIndex - 1)) % playersCards.size(); // Set next player as the winner
			}
			else {
				currentPlayerIndex = (currentPlayerIndex + winnerIndex) % playersCards.size(); // Set next player as the winner
			}
			clearCenterCards(); // Clear the center cards

			scoreUpdated(); // Ensure scores to be updated

			currentTrickIndex++;
			System.out.println("\nTrick winner: Player " + (currentPlayerIndex + 1));
			System.out.println("Player " + (currentPlayerIndex + 1) + " starts the next trick.");
		}
	}

	private int getRankIndex(String rank) {
		String[] rankOrder = { "A", "K", "Q", "J", "X", "9", "8", "7", "6", "5", "4", "3", "2" };

		for (int i = 0; i < rankOrder.length; i++) {
			if (rank.equals(rankOrder[i])) {
				return i;
			}
		}
		return -1; // Invalid rank
	}

	public boolean isGameOver() {
		for (LinkedList<String> playerCards : playersCards) {
			if (playerCards.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void saveGame() throws IOException {
		// save score
		BufferedWriter scoreFile = new BufferedWriter(new FileWriter("playersScores.txt"));

		for (int score : scores.values()) {
			scoreFile.write("\n" + score);
		}

		scoreFile.close();

		// save players card
		BufferedWriter playersCardsFile = new BufferedWriter(new FileWriter("playersCards.txt"));

		for (LinkedList<String> cards : playersCards) {
			StringBuilder sb = new StringBuilder();
			for (String card : cards) {
				sb.append(card).append(", ");
			}
			sb.setLength(sb.length() - 2); // setLength is to adjust length of the stringbuilder
			playersCardsFile.write(sb + "\n");
		}

		playersCardsFile.close();

		// save card deck
		BufferedWriter cardDeckFile = new BufferedWriter(new FileWriter("cardDeck.txt"));

		for (String card : cardDeck) {
			cardDeckFile.write("\n" + card);
		}

		cardDeckFile.close();

		// save current player index turn and trick index
		BufferedWriter playerTrickFile = new BufferedWriter(new FileWriter("playerTrick.txt"));

		playerTrickFile.write("\n" + currentPlayerIndex + ", " + (currentTrickIndex));

		playerTrickFile.close();

		// save center Cards
		BufferedWriter centerCardsFile = new BufferedWriter(new FileWriter("centerCards.txt"));

		for (String card : centerCards) {
			centerCardsFile.write("\n" + card);
		}

		centerCardsFile.close();

		// go back to main menu after saving
		System.out.println("\nThe game is saved. Qutting the game...");
	}

	public void loadGame() throws IOException {
		// Load scores
		scores = new HashMap<>(); // Initialize score to prevent null

		BufferedReader scoreFile = new BufferedReader(new FileReader("playersScores.txt"));
		String line;

		while ((line = scoreFile.readLine()) != null) { // read line by line in a file
			if (!line.isEmpty()) { // check if the line is empty or not
				int score = Integer.parseInt(line); // change from string to int
				scores.put(scores.size(), score);
			}
		}

		scoreFile.close();

		// Load players' cards
		playersCards = new LinkedList<>();

		BufferedReader playersCardsFile = new BufferedReader(new FileReader("playersCards.txt"));

		while ((line = playersCardsFile.readLine()) != null) {
			if (!line.isEmpty()) {
				String[] cards = line.split(", ");
				LinkedList<String> playerCards = new LinkedList<>(Arrays.asList(cards));
				playersCards.add(playerCards);
			}
		}

		playersCardsFile.close();

		// Load card deck
		cardDeck = new HashSet<>();

		BufferedReader cardDeckFile = new BufferedReader(new FileReader("cardDeck.txt"));

		while ((line = cardDeckFile.readLine()) != null) {
			if (!line.isEmpty()) {
				cardDeck.add(line);
			}
		}

		cardDeckFile.close();

		// Load player turn and trick index
		BufferedReader playerTrickFile = new BufferedReader(new FileReader("playerTrick.txt"));

		if ((line = playerTrickFile.readLine()) != null && !line.isEmpty()) {
			String[] data = line.split(", "); // split data in file and put into array
			// store the first element into currentPlayerIndex, change from string to int
			currentPlayerIndex = Integer.parseInt(data[0]);
			// store the second element into currentTrickIndex, change from string to int
			currentTrickIndex = Integer.parseInt(data[1]);
		}

		playerTrickFile.close();

		// Load center cards
		centerCards = new ArrayList<>();

		BufferedReader centerCardsFile = new BufferedReader(new FileReader("centerCards.txt"));

		while ((line = centerCardsFile.readLine()) != null) {
			if (!line.isEmpty()) {
				centerCards.add(line);
			}
		}

		centerCardsFile.close();

		System.out.println("\nLoading the game...");

		while (!isGameOver()) {
			gameStatus();
			playTurn();
		}

		System.out.print("\nGame over!");
		System.out.println("Player " + currentPlayerIndex + " wins the round!");
		System.out.print("Scores        : " );
		for (int i = 0; i < scores.size(); i++) {
			System.out.print("Player " + (i + 1) + " = " + scores.get(i) + " | ");
		}
	}

	public void startGame() throws IOException {
		cardDeck();
		initialScore();
		playersCard();
		setCenterCards();
		playerTurn();

		while (!isGameOver()) {
			gameStatus();
			playTurn();
		}
		
		System.out.print("\nGame over!");
		System.out.println("Player " + currentPlayerIndex + " wins the round!");
		System.out.print("Scores        : " );
		for (int i = 0; i < scores.size(); i++) {
			System.out.print("Player " + (i + 1) + " = " + scores.get(i) + " | ");
		}
	}

	public void playGame() throws IOException {
		while (true) {
			// main menu
			Scanner userCommand = new Scanner(System.in);

			System.out.println("\nWelcome to GoBoom! Let's start!");
			System.out.println("S - Start game");
			System.out.println("R - Resume game");
			System.out.println("X - Exit game");
			System.out.print("\nEnter command: ");

			String choices = userCommand.nextLine().toLowerCase();

			if (choices.equals("s")) {
				startGame();
			}

			else if (choices.equals("r")) {
				loadGame();
			}

			else if (choices.equals("x")) {
				System.out.println("\nGoodbye. Thank you for playing GoBoom! Exiting the program...");
				System.exit(0);
			}

			else {
				System.out.println("\nInvalid command! Please choose one of the above");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		GoBoom game = new GoBoom();
		game.playGame();
	}
}

class Cards {
	protected Set<String> cardDeck;
	protected LinkedList<LinkedList<String>> playersCards;
	protected ArrayList<String> centerCards;
	protected String centerSuit;
	protected int currentPlayerIndex;
	protected int currentTrickIndex;
	protected Map<Integer, Integer> scores;
	protected String leadCard;

	public void cardDeck() {
		cardDeck = new HashSet<>();
		String[] suit = { "H", "D", "C", "S" };
		String[] rank = { "A", "K", "Q", "J", "X", "9", "8", "7", "6", "5", "4", "3", "2" };

		for (int i = 0; i < suit.length; i++) {
			for (int j = 0; j < rank.length; j++) {
				cardDeck.add(suit[i] + rank[j]);
			}
		}

		shuffleDeck(); // shuffle the cards to randomize it
	}

	private void shuffleDeck() {
		List<String> cardDeckList = new ArrayList<>(cardDeck);
		Collections.shuffle(cardDeckList);
		cardDeck = new HashSet<>(cardDeckList);
	}

	// distribute 7 cards to each player
	public void playersCard() {
		playersCards = new LinkedList<>();
		for (int i = 0; i < 4; i++) {
			LinkedList<String> cardPlayer = new LinkedList<>();
			for (int j = 0; j < 7; j++) {
				cardPlayer.add(drawCardFromDeck());
			}
			playersCards.add(cardPlayer);
		}
	}

	// Set the center cards
	public void setCenterCards() {
		centerCards = new ArrayList<>();
		leadCard = drawCardFromDeck();
		centerCards.add(leadCard);
		centerSuit = String.valueOf(leadCard.charAt(0));
	}

	protected boolean isCardPlayable(LinkedList<String> playerCards) {
		for (String card : playerCards) {
			if (isValidPlay(card)) {
				return true;
			}
		}
		return false;
	}

	public void clearCenterCards() {
		centerCards.clear();
	}

public String drawCardFromDeck() {
	if (cardDeck.isEmpty()) {
		System.out.println("The deck is empty. No more cards to draw.");
		System.out.println("Player " + currentPlayerIndex + " is skipped. Moving on to next player ");
		return null;
	}

	Iterator<String> iterator = cardDeck.iterator();
	String card = iterator.next();
	iterator.remove();
	return card;
}

	public boolean isValidPlay(String card) {
    if (centerCards.isEmpty()) {
        return true; // If centerCards is empty, any card can be played
    }

    if (card == null || card.length() < 2) {
        return false; // Invalid card format
    }

    String suit = card.substring(0, 1);
    String rank = card.substring(1);

    String leadCard = centerCards.get(0);
    if (leadCard == null || leadCard.length() < 2) {
        return false; // Invalid lead card format
    }

    String leadSuit = leadCard.substring(0, 1);
    String leadRank = leadCard.substring(1);

    return suit.equals(leadSuit) || rank.equals(leadRank);
}

}
