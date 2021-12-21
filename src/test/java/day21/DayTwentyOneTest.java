package day21;

import org.junit.jupiter.api.Test;

import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

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
		String[] lines = input.split("\n");
		int p1Position = Integer.parseInt(lines[0].split(": ")[1]);
		int p2Position = Integer.parseInt(lines[1].split(": ")[1]);
		int p1Score = 0;
		int p2Score = 0;

		OfInt dice = IntStream.generate(() -> -1)
				.flatMap($ -> IntStream.rangeClosed(1, 100))
				.iterator();

		int diceRolls = 0;
		final long scoreLimit = 1000;
		while (p1Score < scoreLimit && p2Score < scoreLimit) {
			int a = dice.nextInt();
			int b = dice.nextInt();
			int c = dice.nextInt();

			if (diceRolls % 6 < 3) {
				p1Position = nextPosition(p1Position, a + b + c);
				p1Score += p1Position;
				System.out.println("Player 1 rolls %d+%d+%d and moves to space %d for a total score of %d."
						.formatted(a, b, c, p1Position, p1Score));
			} else {
				p2Position = nextPosition(p2Position, a + b + c);
				p2Score += p2Position;
				System.out.println("Player 2 rolls %d+%d+%d and moves to space %d for a total score of %d."
						.formatted(a, b, c, p2Position, p2Score));
			}
			diceRolls += 3;
		}
		return diceRolls * Math.min(p1Score, p2Score);
	}

	private static int nextPosition(int start, int rolled) {
		int value = (start + rolled) % 10;
		return value == 0 ? 10 : value;
	}

}
