package search;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;

public class MixScoreQuery extends CustomScoreQuery{
	static float MAX_INV_PAGE_RANK = (float)1e6;
			
	public MixScoreQuery(Query normalQuery, FunctionQuery pagerankQuery, FunctionQuery clickQuery){
		super(normalQuery, pagerankQuery, clickQuery);
	}
	
	@Override 
	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {  
		return new CustomScoreProvider(context){  
			@Override
			public float customScore(int docId, float subQueryScore, float[] valSrcScores){
				return subQueryScore * (MAX_INV_PAGE_RANK - 1 / valSrcScores[0] + valSrcScores[1]);
			}
		};
	}  
}
