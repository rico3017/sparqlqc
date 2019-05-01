package fr.inrialpes.tyrexmo.queryanalysis;


import java.util.*;

import org.jgrapht.*;

public class CycleDetector<V, E> {
    //~ Instance fields --------------------------------------------------------

    /**
* Graph on which cycle detection is being performed.
*/
    UndirectedGraph<V, E> graph;
    private Set<V> marked;
    private Stack<V> cycle;
    private Map<V,V> edgeTo;
    
    Hashtable <V,Integer> map;

    //~ Constructors -----------------------------------------------------------


    public CycleDetector(UndirectedGraph<V, E> graph)
    {
        this.graph = graph;
        //cycleCheck();
        //System.out.println(cycle);
        this.map = new Hashtable<V,Integer>();
    }

    //~ Methods ----------------------------------------------------------------

  
    
    public boolean hasCycle() {return cycle != null; }
    
    public void cycleCheck() {
    	//if (hasSelfLoop()) return;
    	marked = new HashSet<V>();
    	edgeTo = new HashMap<V,V>();
    	for (V v : this.graph.vertexSet()) {
    		if (!marked.contains(v))
    			dfs(null,v);
    		System.out.println(v);
    	}
    	return;
    }

    private boolean hasSelfLoop() {
    	for (V v : this.graph.vertexSet()) {
    		System.out.println("v = "+ v);
    		for (E e : this.graph.edgesOf(v)) {
    			System.out.println(this.graph.getEdgeTarget(e));
    			if (v == this.graph.getEdgeTarget(e)) {
    				System.out.println("in if "+this.graph.getEdgeTarget(e));
    				cycle = new Stack<V>();
    				cycle.push(v);
    				cycle.push(v);
    				return true;
    			}
    		}
    	}	
    	return false;
    }
    
    private void dfs (V u, V v) {
    	marked.add(v);
    	for(E e : this.graph.edgesOf(v)) {
    		System.out.println(e);
    		V w = this.graph.getEdgeTarget(e);
    		
    		if (cycle != null) return; 
    		
    		if (!marked.contains(w)) {
    			edgeTo.put(w, v);
    			dfs (v,w);
    		} else if (!w.equals(u)) {
    			cycle = new Stack<V>();
    			for (V x = v; !x.equals(w); x = edgeTo.get(x)) {
    				cycle.push(x);
    			}
    			cycle.push(w);
    			cycle.push(v);
    		}
    	}
    }
    public boolean isAcyclic() {
    	Hashtable <V,Integer> parent = new Hashtable <V,Integer> ();
    	for (V v : this.graph.vertexSet())
    		parent.put(v, -1);
    	ArrayList <V> b = new ArrayList<V> ();
    	for (V v : this.graph.vertexSet()) {
    		if (parent.get(v) == -1) {
    			parent.put(v, 0);
    			b.add(v);
    			while (!b.isEmpty()) {
    				//v = b.
    			}
    		}
    	}
    	
    	
    	return true;
    }
    /*
    boolean isAcyclic() 
    {     
      int[] parent = new int[n];
      for (v = 0; v < n; v++)
        parent[v] = -1; // Flag value
      ArrayList b = new ArrayList();
      for (r = 0; r < n; r++)
        if (parent[r] == -1) // r is the root of a new component
        {
          counter++;
          parent[r] = r;
          b.add(r);
          while (b.notEmpty()) 
          {
            v = b.remove();
            for (each neighbor w of v)
              if (parent[w] == -1) // w has not been visited yet
              {
                parent[w] = v;
                b.add(w);
              } 
            else
              if (w != parent[v])
                return false;
          } 
        } 
      return true;
    }*/
    
    public boolean containsCycle() {
    	for (V v : this.graph.vertexSet())
    		map.put(v, -1);
    	
    	for (V v : this.graph.vertexSet()) {
    		if (this.map.get(v) == -1) {
    			if (this.visit(v))
    				return true;
    		}
    	}
    	return false;
    }
    
    private boolean visit(V v) {
    	this.map.put(v, 0);
    	System.out.println("target = "+v);
    	for (E e : this.graph.edgesOf(v)) {
    		if ( v.equals(this.graph.getEdgeSource(e))) return false;
    		else {
    		Integer h = this.map.get(this.graph.getEdgeSource(e));
    		System.out.println("source:" + this.graph.getEdgeSource(e));
    		
    		if(h == 0) {
    			System.out.println("h == 0");
    			return true;
    		}
    		else if (h == -1) {
    			System.out.println("h == -1");
    			if (this.visit(this.graph.getEdgeSource(e))) {
    				return true;
    			}
    		}
    		}
    	}
    	this.map.put(v, 1);
    	return false;
    }
 
 
}

// End CycleDetector.java
