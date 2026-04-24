package util;
import java.util.*;
import java.util.function.Function;
public class Page<T> {
    private final List<T> items;
    private final int pageNumber, pageSize, totalItems;
    public Page(List<T> items, int page, int size, int total) {
        this.items=items; this.pageNumber=page; this.pageSize=size; this.totalItems=total;
    }
    public List<T> getItems()    { return Collections.unmodifiableList(items); }
    public int getTotalPages()   { return (int) Math.ceil((double)totalItems/pageSize); }
    public boolean hasNext()     { return pageNumber < getTotalPages()-1; }
    public boolean hasPrevious() { return pageNumber > 0; }
    public int getPageNumber()   { return pageNumber; }
    public int getTotalItems()   { return totalItems; }
    public <R> Page<R> map(Function<T,R> fn) {
        List<R> m=new ArrayList<>(); for(T i:items) m.add(fn.apply(i));
        return new Page<>(m,pageNumber,pageSize,totalItems);
    }
    @Override public String toString() {
        return "Page{"+pageNumber+"/"+(getTotalPages()-1)+", "+items.size()+"/"+totalItems+"}";
    }
}
