package tools.argumentTainter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import neo4j.readWriteDB.Neo4JDBInterface;
import neo4j.traversals.readWriteDB.Traversals;

import org.neo4j.graphdb.Node;

// Determine functions to patch and hand over
// individual functions to FunctionPatcher

public class ArgumentTainter
{

	HashMap<Long, CallsForFunction> sourceCallsByFuncId;
	Collection<Long> functionsToPatch = new HashSet<Long>();
	FunctionPatcher functionPatcher = new FunctionPatcher();
	private String source;

	public void initialize(String databaseDir)
	{
		Neo4JDBInterface.setDatabaseDir(databaseDir);
		Neo4JDBInterface.openDatabase();
	}

	public void setSourceToPatch(String sourceToPatch)
	{
		source = sourceToPatch;
//System.out.println("sourceToPatch: " + sourceToPatch);
		functionPatcher.setSourceToPatch(sourceToPatch);
	}

	public void setArgToPatch(int taintedArg)
	{
//System.out.println("argToPatch: " + taintedArg);
		functionPatcher.setArgumentToPatch(taintedArg);
	}

	public void patch()
	{
		determineFunctionsToPatch(source);	
//System.out.println("mr> set patched_functions");
//System.out.println("mr> end patched_functions");
		
		for (Long funcId : functionsToPatch)
		{
//System.out.println("\nmr> patched_functions patching function with id: " + funcId.toString());
			patchFunction(funcId);
		
		}
	}

	private void determineFunctionsToPatch(String source)
	{
		List<Node> hits = Traversals.getCallsTo(source);
		for (Node callASTNode : hits)
		{
			Long functionId = Traversals.getFunctionIdFromASTNode(callASTNode);
			functionsToPatch.add(functionId);	
		}
	
	}

	public void patchFunction(Long funcId)
	{
		functionPatcher.reset();
		functionPatcher.patch(funcId);
	}

	public void shutdown()
	{
		Neo4JDBInterface.closeDatabase();
	}

}
