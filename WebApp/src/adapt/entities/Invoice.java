package adapt.entities;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.CascadeType;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;

   /** 
   Class generated using Kroki EJBGenerator 
   @Author mrd 
   Creation date: 28.03.2013  12:21:09h
   **/

@Entity
@Table(name = "WS_INVOICE")
public class Invoice implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id 
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private java.lang.Long id;

	@Column(name = "INV_INVOICE_NUMBER", unique = false, nullable = false)
	private java.lang.String name;
	
	@Column(name = "INV_INVOICE_DATE", unique = false, nullable = false)
	private java.util.Date invoiceDate;
	
	@Column(name = "INV_TOTAL", unique = false, nullable = false)
	private java.math.BigDecimal total;
	
	
	@ManyToOne
	@JoinColumn(name="orders", referencedColumnName="ID", nullable=true)
	private Orders orders;
	
	
	public Invoice(){
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public java.lang.String getName() {
		return this.name;
	}
	
	public void setName(java.lang.String name) {
		this.name = name;
	}
	
	public java.util.Date getInvoiceDate() {
		return this.invoiceDate;
	}
	
	public void setInvoiceDate(java.util.Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	
	public java.math.BigDecimal getTotal() {
		return this.total;
	}
	
	public void setTotal(java.math.BigDecimal total) {
		this.total = total;
	}
	
	public Orders getOrders() {
		return this.orders;
	}
	
	public void setOrders(Orders orders) {
		this.orders = orders;
	}
	
}