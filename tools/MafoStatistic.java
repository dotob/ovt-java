package tools;

import java.util.Vector;

import db.Gespraech;
import db.Marktforscher;

public class MafoStatistic implements Runnable{

	private Marktforscher mafo;
	private ContactBundle finishedContacts;
	private ContactBundle waitingContacts;
	private ContactBundle noMoneyContacts;
	private ContactBundle providedContacts;
	
	private DateInterval finishedRange;
	private DateInterval noMoneyRange;
	private DateInterval providedRange;
	
	public MafoStatistic(Marktforscher mafo){
		this.mafo = mafo;
	}
	
	public MafoStatistic(Marktforscher mafo, DateInterval range){
		this.finishedRange = range;
		this.noMoneyRange  = range;
		this.providedRange = range;
		this.mafo = mafo;
	}
	
	public ContactBundle getFinishedContacts(DateInterval range){
		ContactBundle ret = null;
		if (range!=null){
			if (this.finishedContacts==null || !this.finishedRange.equals(range)){
				this.finishedRange = range;
				this.finishedContacts = this.mafo.getFinishedContacts(range);
				ret = this.finishedContacts;
			}
		}
		return ret;
	}
	
	public ContactBundle getFinishedContacts(){
		return this.getFinishedContacts(this.finishedRange);
	}
	
	public ContactBundle getWaitingContacts(){
		ContactBundle ret = null;
		if (this.waitingContacts==null){
			this.waitingContacts = this.mafo.getWaitingContacts();
			ret = this.waitingContacts;
		}
		return ret;
	}
	
	public ContactBundle getNoMoneyContacts(DateInterval range){
		ContactBundle ret = null;
		if (range!=null){
			if (this.noMoneyContacts==null || !this.noMoneyContacts.equals(range)){
				this.noMoneyRange = range;
				this.noMoneyContacts = this.mafo.getNoMoneyContacts(range);
				ret = this.noMoneyContacts;
			}
		}
		return ret;
	}
	
	public ContactBundle getNoMoneyContacts(){
		return this.getNoMoneyContacts(this.noMoneyRange);
	}
	
	public ContactBundle getProvidedContacts(DateInterval range){
		if (this.providedContacts==null || !this.providedRange.equals(range)){
			this.providedRange = range;
			this.providedContacts = new ContactBundle();
			this.providedContacts.addBundle(this.getFinishedContacts(range));
			this.providedContacts.addBundle(this.getWaitingContacts());
			this.providedContacts.addBundle(this.getNoMoneyContacts(range));
		}
		return this.providedContacts;
	}

	public ContactBundle getProvidedContacts(){
		return this.getProvidedContacts(this.providedRange);
	}
	
	public Vector<Gespraech> getHonTermine(DateInterval range){
		Vector<Gespraech> ret = null;
		
		return ret;
	}
	
	public void run() {
		this.getFinishedContacts();
		this.getWaitingContacts();
		this.getNoMoneyContacts();
		this.getProvidedContacts();
	}
	
	
}
