package day21;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

class DayTwentyOneTest {

	@Test
	void partOneSample() throws Exception {
		assertThat(multiplyLoserScoreWithDiceRolls("""
				Player 1 starting position: 4
				Player 2 starting position: 8
				""")).isEqualByComparingTo(739785);
	}

	@Test
	void partOneInput() throws Exception {
		assertThat(multiplyLoserScoreWithDiceRolls("""
				Player 1 starting position: 1
				Player 2 starting position: 10
				""")).isEqualByComparingTo(428736);
	}

	private static int multiplyLoserScoreWithDiceRolls(String input) {
		OfInt dice = IntStream.generate(() -> -1)
				.flatMap($ -> IntStream.rangeClosed(1, 100))
				.iterator();

		GameState gameState = parse(input);
		int diceRolls = 0;
		while (!gameState.hasWon(1000)) {
			int a = dice.nextInt();
			int b = dice.nextInt();
			int c = dice.nextInt();
			diceRolls += 3;
			gameState = gameState.newState(a + b + c);
		}
		return diceRolls * Math.min(gameState.p1Score(), gameState.p2Score());
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(howManyUniverseWins("""
				Player 1 starting position: 4
				Player 2 starting position: 8
				""")).isEqualByComparingTo(444356092776315L);
	}

	@Test
	void partTwoInput() throws Exception {
		assertThat(howManyUniverseWins("""
				Player 1 starting position: 1
				Player 2 starting position: 10
				""")).isEqualByComparingTo(57328067654557L);
	}

	private static long howManyUniverseWins(String input) {
		Map<GameState, Long> universeCopies = new HashMap<>();
		universeCopies.put(parse(input), 1L);

		int scoreLimit = 21;
		while (!universeCopies.keySet().stream().allMatch(state -> state.hasWon(scoreLimit))) {
			Map<GameState, Long> nextCopies = new HashMap<>();
			for (Entry<GameState, Long> previousEntry : universeCopies.entrySet()) {
				GameState previousState = previousEntry.getKey();
				Long previousCount = previousEntry.getValue();
				if (previousState.hasWon(scoreLimit)) {
					nextCopies.merge(previousState, previousCount, Long::sum);
				} else {
					for (Map.Entry<GameState, Long> nextEntry : previousState.split().entrySet()) {
						nextCopies.merge(nextEntry.getKey(), nextEntry.getValue() * previousCount, Long::sum);
					}
				}
			}
			universeCopies = nextCopies;
		}

		return Math.max(
				universeCopies.entrySet().stream().filter(e -> !e.getKey().playerOneTurn())
						.mapToLong(Map.Entry::getValue).sum(),
				universeCopies.entrySet().stream().filter(e -> e.getKey().playerOneTurn())
						.mapToLong(Map.Entry::getValue).sum());
	}

	private static GameState parse(String input) {
		String[] lines = input.split("\n");
		int p1Position = Integer.parseInt(lines[0].split(": ")[1]);
		int p2Position = Integer.parseInt(lines[1].split(": ")[1]);
		return new GameState(p1Position, 0, p2Position, 0, true);
	}

}

record GameState(
		int p1Position, int p1Score,
		int p2Position, int p2Score,
		boolean playerOneTurn) {

	Map<GameState, Long> split() {
		return IntStream.rangeClosed(1, 3)
				.mapToObj(a -> IntStream.rangeClosed(1, 3)
						.mapToObj(b -> IntStream.rangeClosed(1, 3)
								.mapToObj(c -> newState(a + b + c))))
				.flatMap(identity())
				.flatMap(identity())
				.collect(groupingBy(identity(), counting()));
	}

	public GameState newState(int rolled) {
		if (playerOneTurn) {
			int nextPosition = nextPosition(p1Position, rolled);
			return new GameState(
					nextPosition, p1Score + nextPosition,
					p2Position, p2Score,
					false);
		}
		int nextPosition = nextPosition(p2Position, rolled);
		return new GameState(
				p1Position, p1Score,
				nextPosition, p2Score + nextPosition,
				true);
	}

	private static int nextPosition(int start, int rolled) {
		int next = (start + rolled) % 10;
		return next == 0 ? 10 : next;
	}

	public boolean hasWon(int scoreLimit) {
		return scoreLimit <= p1Score || scoreLimit <= p2Score;
	}

}
