package search;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;

public class MixScoreQuery extends CustomScoreQuery{
	static double cyc = 500;
	static double ratio = 10000;
	static double exp = 0.25;
	
	int tot, sum;
	
	public MixScoreQuery(Query q0, FunctionQuery q1, FunctionQuery q2, int tot, int sum){
		super(q0, q1, q2);
		this.tot = tot;
		this.sum = sum;
	}
	
	@Override 
	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {  
		return new CustomScoreProvider(context){  
			@Override
			public float customScore(int docId, float subQueryScore, float[] valSrcScores){
				double bm25 = subQueryScore;
				double pagerank = valSrcScores[0];
				double click = valSrcScores[1];
				double newrank = (cyc * tot * pagerank + click * ratio)/(cyc * tot + ratio * sum);
				return (float)(bm25 * Math.pow(newrank, exp));
			}
		};
	}  
}
