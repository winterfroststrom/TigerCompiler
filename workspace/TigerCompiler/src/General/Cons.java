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
}
