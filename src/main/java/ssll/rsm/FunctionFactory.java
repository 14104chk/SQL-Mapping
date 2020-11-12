package ssll.rsm;

import ssll.rsm.fn.Function;

public interface FunctionFactory {

	public Function build(MappingContext context, String[] args) throws MappingException;
}
