package day04;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayFourTest {

	private static final String SAMPLE = """
			7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1

			22 13 17 11  0
			 8  2 23  4 24
			21  9 14 16  7
			 6 10  3 18  5
			 1 12 20 15 19

			 3 15  0  2 22
			 9 18 13 17  5
			19  8  7 25 23
			20 11 10 24  4
			14 21 16 12  6

			14 21 17 24  4
			10 16 15  9 19
			18  8 23 26 20
			22 11 13  6  5
			 2  0 12  3  7
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(Game.parse(SAMPLE).scoreBestBoard()).isEqualTo(4512);
	}

	@Test
	void partOneInput() throws Exception {
		String lines = Files.lines(Paths.get(getClass().getResource("input").toURI()))
				.collect(Collectors.joining("\n"));
		assertThat(Game.parse(lines).scoreBestBoard()).isEqualTo(34506);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(Game.parse(SAMPLE).scoreWorstBoard()).isEqualTo(1924);
	}

	@Test
	void partTwoInput() throws Exception {
		String lines = Files.lines(Paths.get(getClass().getResource("input").toURI()))
				.collect(Collectors.joining("\n"));
		assertThat(Game.parse(lines).scoreWorstBoard()).isEqualTo(7686);
	}

}

record Game(
		int[] draws,
		List<BingoCard> cards) {

	static Game parse(String input) {
		String[] split = input.split("\n\n");
		return new Game(
				Stream.of(split[0].split(",")).mapToInt(Integer::parseInt).toArray(),
				Stream.of(split).skip(1).map(BingoCard::parse).toList());
	}

	int scoreBestBoard() {
		return IntStream.of(draws)
				.flatMap(draw -> cards.stream()
						.peek(card -> card.mark(draw))
						.filter(BingoCard::hasWon)
						.mapToInt(card -> draw * card.sumUnmarked()))
				.findFirst()
				.getAsInt();
	}

	int scoreWorstBoard() {
		return IntStream.of(draws)
				.flatMap(draw -> cards.stream()
						.peek(card -> card.mark(draw))
						.filter(BingoCard::hasWon)
						.mapToInt(card -> draw * card.sumUnmarked()))
				.dropWhile(score -> !cards.stream().allMatch(BingoCard::hasWon))
				.findFirst()
				.getAsInt();
	}

}

record BingoCard(Map<Position, State> positions, int rows, int columns) {

	static BingoCard parse(String board) {
		Map<Position, State> positions = new HashMap<>();

		String[] split = board.split("\n");
		for (int row = 0; row < split.length; row++) {
			int[] rowNumbers = Stream.of(split[row].trim().split("\s+"))
					.mapToInt(Integer::parseInt)
					.toArray();
			for (int column = 0; column < rowNumbers.length; column++) {
				positions.put(new Position(row, column), new State(rowNumbers[column], false));
			}
		}
		return new BingoCard(
				positions,
				split.length,
				positions.keySet().stream().mapToInt(Position::column).max().getAsInt());
	}

	void mark(int number) {
		positions.replaceAll((t, u) -> u.number() == number ? new State(number, true) : u);
	}

	boolean hasWon() {
		return IntStream.range(0, rows)
				.anyMatch(row -> allMarked(entry -> entry.getKey().row() == row))
				|| IntStream.range(0, columns)
						.anyMatch(column -> allMarked(entry -> entry.getKey().column() == column));
	}

	private boolean allMarked(Predicate<Entry<Position, State>> predicate) {
		return positions.entrySet().stream().filter(predicate).allMatch(entry -> entry.getValue().marked());
	}

	int sumUnmarked() {
		return positions.values().stream().filter(Predicate.not(State::marked)).mapToInt(State::number).sum();
	}

}

record Position(int row, int column) {
}

record State(int number, boolean marked) {
}
