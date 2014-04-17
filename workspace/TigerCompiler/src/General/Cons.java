package General;

public class Cons<A, B> {
	public A a;
	public B b;
	
	public Cons(A a, B b){
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String toString(){
		return "(" + a + ", " + b + ")";
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Cons){
			return ((Cons) o).a.equals(a) && ((Cons) o).b.equals(b);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return a.hashCode() ^ b.hashCode();
	}
}
