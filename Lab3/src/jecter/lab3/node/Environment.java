package jecter.lab3.node;

import java.util.HashSet;
import java.util.Set;

public class Environment {
    private final Set<Neighbour> neighbours = new HashSet<>();
    private final Substitute substitute = new Substitute();


    public void addNeighbour(Neighbour neighbour) {
        neighbours.add(neighbour);
        if (!substitute.exists()) {
            substitute.setAddress(neighbour);
        }
        System.out.println("[NEIGHBOUR <" + neighbour.getName() + "> ADDED]");
    }

    public void removeNeighbour(Neighbour neighbour) {
        neighbours.remove(neighbour);
        if (substitute.exists() && substitute.getAddress().equals(neighbour.getAddress())) {
            if (!neighbours.isEmpty()) {
                substitute.setAddress(neighbours.iterator().next());
            } else {
                substitute.remove();
            }
        }
        System.out.println("[NEIGHBOUR <" + neighbour.getName() + "> REMOVED]");
    }

    public Substitute getSubstitute() {
        return substitute;
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
