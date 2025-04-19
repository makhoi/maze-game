/*
 * SimpleMazeGame.java
 * Copyright (c) 2008, Drexel University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Drexel University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY DREXEL UNIVERSITY ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DREXEL UNIVERSITY BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package maze;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import maze.ui.MazeViewer;

/**
 *
 * @author Sunny
 * @version 1.0
 * @since 1.0
 */
public class SimpleMazeGame {
	/**
	 * Creates a small maze.
	 */
	public static Maze createMaze() {
		Maze maze = new Maze();

		// Create room
		Room room0 = new Room(0);
		Room room1 = new Room(1);

		// Create doors between room
		Door door = new Door(room0, room1);

		// Set walls for room0
		room0.setSide(Direction.North, new Wall());
		room0.setSide(Direction.South, new Wall());
		room0.setSide(Direction.East, door);
		room0.setSide(Direction.West, new Wall());

		// Set sides for room1
		room1.setSide(Direction.North, new Wall());
		room1.setSide(Direction.South, new Wall());
		room1.setSide(Direction.East, new Wall());
		room1.setSide(Direction.West, new Wall());

		// Add rooms to the maze
		maze.addRoom(room0);
		maze.addRoom(room1);

		// Set current room to room0
		maze.setCurrentRoom(room0);

		return maze;
	}

	public static Maze loadMaze(final String path) {
		Maze maze = new Maze();

		Map<Integer, Room> roomMap = new HashMap<>();
		Map<String, String[]> pendingDoors = new HashMap<>();
		Map<String, Door> doorMap = new HashMap<>();
		Map<Integer, String[]> roomSides = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;

			// First pass: Create rooms and save side descriptors / door info
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}

				String[] parts = line.trim().split(" ");

				if (parts[0].equals("room")) {
					int roomNumber = Integer.parseInt(parts[1]);
					Room room = new Room(roomNumber);
					roomMap.put(roomNumber, room);

					String[] sides = Arrays.copyOfRange(parts, 2, 6);
					roomSides.put(roomNumber, sides);

				} else if (parts[0].equals("door")) {
					String doorId = parts[1];
					pendingDoors.put(doorId, parts);
				}
			}

			// Second pass: Now create Door objects, all rooms are guaranteed to exist
			for (Map.Entry<String, String[]> entry : pendingDoors.entrySet()) {
				String doorId = entry.getKey();
				String[] parts = entry.getValue();

				int r1 = Integer.parseInt(parts[2]);
				int r2 = Integer.parseInt(parts[3]);
				boolean open = parts.length > 4 && parts[4].equalsIgnoreCase("open");

				Room room1 = roomMap.get(r1);
				Room room2 = roomMap.get(r2);

				Door door = new Door(room1, room2);
				door.setOpen(open);
				doorMap.put(doorId, door);
			}

			// Third pass: Set room sides
			for (Map.Entry<Integer, String[]> entry : roomSides.entrySet()) {
				int roomNum = entry.getKey();
				Room room = roomMap.get(roomNum);
				String[] sides = entry.getValue();

				for (int i = 0; i < 4; i++) {
					Direction dir = Direction.values()[i];
					String side = sides[i];

					if (side.equals("wall")) {
						room.setSide(dir, new Wall());
					} else if (side.startsWith("d")) {
						Door door = doorMap.get(side);
						room.setSide(dir, door);
					} else {
						try {
							Integer.parseInt(side); // ignoring raw room connections
							room.setSide(dir, new Wall());
						} catch (NumberFormatException e) {
							room.setSide(dir, new Wall());
						}
					}
				}

				maze.addRoom(room);
			}

			// Set starting room to room 0
			if (roomMap.containsKey(0)) {
				maze.setCurrentRoom(roomMap.get(0));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return maze;
	}

	public static void main(String[] args) {
		Maze maze = loadMaze("/Users/khoima/Desktop/Spring2425/SE310/week2/HW1/large.maze");
		MazeViewer viewer = new MazeViewer(maze);
		viewer.run();
	}
}
