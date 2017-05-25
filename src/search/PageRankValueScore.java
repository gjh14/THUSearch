package search;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.valuesource.FieldCacheSource;

public class PageRankValueScore extends FieldCacheSource {
	public PageRankValueScore() {  
		super("pagerank");
    }

	@Override
	public FunctionValues getValues(Map context, LeafReaderContext leafReaderContext) throws IOException {
		final NumericDocValues numericDocValues = DocValues.getNumeric(leafReaderContext.reader(), field);
		return new FunctionValues(){
				@Override
				public String toString(int doc) {
					return description() + '=' + intVal(doc);
				}
				@Override
				public float floatVal(int doc){
					return Float.intBitsToFloat((int) numericDocValues.get(doc));
				}
			};
	}
}
