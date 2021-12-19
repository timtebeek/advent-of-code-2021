package day19;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class DayNineteenTest {

	@Test
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		assertThat(Mapper.assembleAndAnswer(scanners, false)).isEqualTo(79);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		List<Scanner> scanners = Parser.parse(input);
		assertThat(Mapper.assembleAndAnswer(scanners, false)).isEqualTo(408);
	}

	@Test
	void orientationTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("orientations").toURI()));
		List<Scanner> orientations = Parser.parse(sample);
		assertThat(orientations.get(0).orientations())
				.hasSize(24)
				.containsAll(orientations);
	}

	@Test
	void overlappingTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		String overlapping = scanners.stream().flatMap(left -> Mapper.overlappingScannerPairs(left, scanners))
				.map(ScannerPair::toString).collect(joining(", "));
		assertThat(overlapping).isEqualTo("0-1, 0-4, 1-2, 1-3, 1-4, 2-4, 3-4");
	}

	@Test
	void rightScannerPositionTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		ScannerPair scannerPair = Mapper.overlappingScannerPairs(scanners.get(0), scanners).findFirst().get();
		assertThat(scannerPair.rightScannerPosition()).isEqualByComparingTo(new Position(68, -1246, -43));
	}

	@Test
	void partTwoSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		assertThat(Mapper.assembleAndAnswer(scanners, true)).isEqualTo(3621);
	}

	@Test
	void partTwoInput() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		assertThat(Mapper.assembleAndAnswer(scanners, true)).isEqualTo(13348);
	}

}

class Parser {

	public static List<Scanner> parse(String input) {
		return Stream.of(input.split("\n\n")).map(Parser::scanner).toList();
	}

	private static Scanner scanner(String block) {
		String[] lines = block.split("\n");
		int id = Integer.parseInt(lines[0].split(" ")[2]);
		return new Scanner(id, Stream.of(lines).skip(1).map(Parser::position).collect(toSet()));
	}

	private static Position position(String line) {
		int[] values = Stream.of(line.split(",")).mapToInt(Integer::parseInt).toArray();
		return new Position(values[0], values[1], values[2]);
	}

}

class Mapper {

	public static int assembleAndAnswer(List<Scanner> scanners, boolean partTwo) {
		List<Scanner> scannersToAddToMap = new ArrayList<>(scanners.subList(1, scanners.size())); // Mutable list
		List<Position> scannerPositions = new ArrayList<>(List.of(new Position(0, 0, 0)));
		Scanner map = new Scanner(-1, new HashSet<>(scanners.get(0).beacons()));
		while (!scannersToAddToMap.isEmpty()) {
			for (ScannerPair scannerPair : overlappingScannerPairs(map, scannersToAddToMap).toList()) {
				map.beacons().addAll(scannerPair.rightBeaconsViewedFromLeft().toList());
				scannersToAddToMap.removeIf(scanner -> scanner.id() == scannerPair.right().id());
				scannerPositions.add(scannerPair.rightScannerPosition());
			}
		}
		return partTwo ? largestManhattenDistanceBetweenScanners(scannerPositions) : map.beacons().size();
	}

	private static int largestManhattenDistanceBetweenScanners(List<Position> scannerPositions) {
		return scannerPositions.stream()
				.flatMap(left -> scannerPositions.stream()
						.filter(right -> left.compareTo(right) < 0)
						.map(right -> new PositionPair(left, right)))
				.mapToInt(pair -> pair.distance().manhattan())
				.max()
				.getAsInt();
	}

	static Stream<ScannerPair> overlappingScannerPairs(Scanner left, List<Scanner> scanners) {
		return scanners.stream()
				.flatMap(right -> right.orientations().stream())
				.map(orientation -> new ScannerPair(left, orientation))
				.filter(pair -> pair.left().id() < pair.right().id())
				.filter(ScannerPair::overlap);
	}

}

record ScannerPair(Scanner left, Scanner right) {

	boolean overlap() {
		Set<Position> intersection = left.distancePairs().keySet();
		intersection.retainAll(right.distancePairs().keySet());
		return 12 < intersection.size();
	}

	Position rightScannerPosition() {
		Map<Position, PositionPair> leftDistancePairs = left.distancePairs();
		Map<Position, PositionPair> rightDistancePairs = right.distancePairs();
		Map.Entry<Position, PositionPair> firstRightEntry = rightDistancePairs.entrySet().stream()
				.filter(entry -> leftDistancePairs.containsKey(entry.getKey()))
				.findFirst()
				.get();
		Position firstLeft = leftDistancePairs.get(firstRightEntry.getKey()).left();
		Position firstRight = firstRightEntry.getValue().left();
		return new Position(
				firstLeft.x() - firstRight.x(),
				firstLeft.y() - firstRight.y(),
				firstLeft.z() - firstRight.z());
	}

	Stream<Position> rightBeaconsViewedFromLeft() {
		Position delta = rightScannerPosition();
		return right.beacons().stream()
				.map(p -> new Position(
						p.x() + delta.x(),
						p.y() + delta.y(),
						p.z() + delta.z()));
	}

	@Override
	public String toString() {
		return "%d-%d".formatted(left.id(), right.id());
	}

}

record Scanner(int id, Set<Position> beacons) {

	public Set<Scanner> orientations() {
		return Stream.of(this, this.flip())
				.flatMap(scanner -> Stream.iterate(scanner, Scanner::clockwiseTurn).limit(4))
				.flatMap(scanner -> Stream.iterate(scanner, Scanner::spin).limit(3))
				.collect(toSet());
	}

	private Scanner clockwiseTurn() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.y(), -p.x(), p.z())).collect(toSet()));
	}

	private Scanner spin() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.y(), p.z(), p.x())).collect(toSet()));
	}

	private Scanner flip() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.x(), -p.y(), -p.z())).collect(toSet()));
	}

	public Map<Position, PositionPair> distancePairs() {
		return beacons.stream()
				.flatMap(left -> beacons.stream()
						.filter(right -> left.compareTo(right) < 0)
						.map(right -> new PositionPair(left, right)))
				.collect(toMap(PositionPair::distance, Function.identity()));
	}

	@Override
	public String toString() {
		return """
				--- scanner %d ---
				%s
				""".formatted(id, beacons.stream().map(Position::toString).collect(joining("\n")));
	}

}

record PositionPair(Position left, Position right) {

	Position distance() {
		return new Position(
				right.x() - left.x(),
				right.y() - left.y(),
				right.z() - left.z());
	}

}

record Position(int x, int y, int z) implements Comparable<Position> {

	public int manhattan() {
		return Math.abs(x) + Math.abs(y) + Math.abs(z);
	}

	@Override
	public String toString() {
		return "%d,%d,%d".formatted(x, y, z);
	}

	@Override
	public int compareTo(Position other) {
		return comparing(Position::x)
				.thenComparing(Position::y)
				.thenComparing(Position::z)
				.compare(this, other);
	}

}
