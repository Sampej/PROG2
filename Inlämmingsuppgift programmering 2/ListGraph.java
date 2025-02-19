//PROG2 VT2023, Inlämningsuppgift, del 1


import java.io.Serializable;
import java.util.*;


public class ListGraph<T> implements Graph<T>, Serializable {

    private final Map<T, Set<Edge>> nodes = new HashMap<>();

    @Override
    public void add(Object node) {
        nodes.putIfAbsent((T)node, new HashSet<>());
    }

    @Override
    public void connect(Object nodeFrom, Object nodeTo, String name, int weight){
        if(getNodes().contains(nodeFrom) && getNodes().contains(nodeTo) && weight>0){
            if(getEdgeBetween(nodeFrom, nodeTo) != null){
                throw new IllegalStateException("\nEdge already exists between these nodes with the same name and weight = "+ getEdgeBetween(nodeFrom, nodeTo) + "\n");

            }else{
                if(getEdgesFrom(nodeFrom).contains(new Edge<>(nodeTo, weight, name)) || getEdgesFrom(nodeTo).contains(new Edge<>(nodeFrom, weight, name)) ){
                    throw new IllegalStateException("\nEdge already exists between these nodes with the same name and weight\n");
                }

                nodes.get(nodeTo).add(new Edge<>(nodeFrom, weight, name));
                nodes.get(nodeFrom).add(new Edge<>(nodeTo, weight, name));
            }
        }else{
            if(weight<0){
                throw new IllegalArgumentException("Weight can´t be negative");
            }else{
                throw new NoSuchElementException();
            }
        }
    }

    @Override
    public void setConnectionWeight(Object node1, Object node2, int weight) {
        if(nodesExists(node1, node2)){
            if (weight < 0) {
                throw new IllegalArgumentException("Weight can´t be negative.");
            }
            getEdgeBetween(node1, node2).setWeight(weight);
            getEdgeBetween(node2, node1).setWeight(weight);

        }else{
            throw new NoSuchElementException();
        }

    }

    @Override
    public Set getNodes() {
        return new HashSet<>(nodes.keySet());
    }



    @Override
    public Collection<Edge> getEdgesFrom(Object node) {
        if (getNodes().contains(node)) {
            return new HashSet<>(nodes.get(node));
        }
        throw new NoSuchElementException();
    }


    @Override
    public Edge getEdgeBetween(Object next, Object current) {
        if (getNodes().contains(next) && getNodes().contains(current)) {
            for (var edge : getEdgesFrom(next)) {
                if (edge.getDestination() == current && edge.getDestination().toString().equals(current.toString())) {
                    return edge;
                }
            }
            return null;
        } else {
            throw new NoSuchElementException();
        }
    }



    @Override
    public void disconnect(Object node1, Object node2) {
        if (!nodesExists(node1, node2)) {
            throw new NoSuchElementException();
        }
        var e = getEdgeBetween(node2, node1);
        if (e == null) {
            throw new IllegalStateException();
        }
        nodes.get(node1).removeIf(edge -> edge.getName().equals(e.getName()));
        nodes.get(node2).removeIf(edge -> edge.getName().equals(e.getName()));
    }

    @Override
    public void remove(Object node) {
        if (getEdgesFrom(node) != null) {
            for (var e : getEdgesFrom(node)) {
                if (getEdgesFrom(e.getDestination()) != null) {
                    nodes.get(e.getDestination()).removeIf(edge -> edge.getName().equals(e.getName()));
                }
            }
            nodes.remove(node);
        } else {
            throw new NoSuchElementException("The node does not exist in the graph.");
        }
    }

    @Override
    public boolean pathExists(Object from, Object to) {
        if (nodesExists(from, to)) {
            if (getEdgesFrom(from).isEmpty() || getEdgesFrom(to).isEmpty()) {
                return false;
            }
            if (getEdgeBetween(to, from) != null) {
                return true;
            }
            return !getPath(to, from).isEmpty();
        }
        return false;
    }

    @Override
    public List<Edge> getPath(Object from, Object to) {
        if (getEdgesFrom(from).isEmpty() || getEdgesFrom(to).isEmpty()) {
            return null;
        }

        Map<Object, Integer> distance = new HashMap<>();
        Map<Object, Object> previous = new HashMap<>();
        PriorityQueue<Object> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));
        Set<Object> visited = new HashSet<>();

        distance.put(from, 0);
        previous.put(from, null);
        queue.add(from);

        while (!queue.isEmpty()) {
            Object current = queue.poll();
            visited.add(current);

            if (current.equals(to)) {
                return getEdgePath(previous, to);
            }

            for (var edge : getEdgesFrom(current)) {
                Object neighbor = edge.getDestination();
                int weight = edge.getWeight();
                int altDistance = distance.get(current) + weight;

                if (!visited.contains(neighbor) && (!distance.containsKey(neighbor) || altDistance < distance.get(neighbor))) {
                    distance.put(neighbor, altDistance);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return null;
    }

    private List<Edge> getEdgePath(Map<Object, Object> previous, Object to) {
        List<Edge> edgePath = new ArrayList<>();
        Object current = to;
        while (previous.get(current) != null) {
            Object previousNode = previous.get(current); 

            for (var edge : getEdgesFrom(previousNode)) {
                if (edge.getDestination().equals(current)) {
                    edgePath.add(0, edge);
                    break;
                }
            }
            current = previousNode;
        }
        return edgePath;
    }




    @Override
    public String toString() {
        List<T> keySet = new ArrayList<>(nodes.keySet());
        StringBuilder str = new StringBuilder();
        for (T t : keySet) {
            str.append("\n").append(t).append(" ").append(getEdgesFrom(t)).append("\n").append("--> Total = ").append(nodes.get(t).size()).append(" edges\n");
        }
        return str.toString();
    }

    private boolean nodesExists(Object nextNode, Object currentNode) {
        return getNodes().contains(currentNode) && getNodes().contains(nextNode);
    }


}
