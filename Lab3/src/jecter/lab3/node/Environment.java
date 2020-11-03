package jecter.lab3.node;

import java.util.HashSet;
import java.util.Set;

public class Environment {
    private final Set<Neighbour> neighbours = new HashSet<>();

    public void addNeighbour(Neighbour neighbour) {
        neighbours.add(neighbour);
        System.out.println("[NEIGHBOUR <" + neighbour.getName() + "> ADDED]");
    }

    public void removeNeighbour(Neighbour neighbour) {
        neighbours.remove(neighbour);
        System.out.println("[NEIGHBOUR <" + neighbour.getName() + "> REMOVED]");
    }

    public Set<Neighbour> getNeighbours() {
        return new HashSet<>(neighbours);
    }

    public Neighbour findNeighbour(Neighbour equalNeighbour) {
        Set<Neighbour> neighbours = getNeighbours();
        for (var neighbour : neighbours) {
            if (neighbour.equals(equalNeighbour)) {
                return neighbour;
            }
        }
        throw new RuntimeException();
    }
}
