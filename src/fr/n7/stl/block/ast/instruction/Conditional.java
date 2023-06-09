/**
 * 
 */
package fr.n7.stl.block.ast.instruction;

import java.util.Optional;

import fr.n7.stl.block.ast.Block;
import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.expression.Expression;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.block.ast.type.AtomicType;
import fr.n7.stl.block.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Implementation of the Abstract Syntax Tree node for a conditional instruction.
 * @author Marc Pantel
 *
 */
public class Conditional implements Instruction {

	protected Expression condition;
	protected Block thenBranch;
	protected Block elseBranch;

	public Conditional(Expression _condition, Block _then, Block _else) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = _else;
	}

	public Conditional(Expression _condition, Block _then) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "if (" + this.condition + " )" + this.thenBranch + ((this.elseBranch != null)?(" else " + this.elseBranch):"");
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		boolean o3 = true;
		boolean o1 = condition.collectAndBackwardResolve(_scope);
		boolean o2 = thenBranch.collect(_scope);
		if (elseBranch != null) {
			o3 = elseBranch.collect(_scope);
		}
		return o1 && o2 && o3;
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		boolean o3 = true;
		boolean o1 = condition.fullResolve(_scope);
		boolean o2 = thenBranch.resolve(_scope);
		if (elseBranch != null) {
			o3 = elseBranch.resolve(_scope);
		}
		return o1 && o2 && o3;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		Type te = condition.getType();
		if (te instanceof AtomicType && te.equalsTo(AtomicType.BooleanType)) {
			return true;
		}
		return false;
	}
	

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#allocateMemory(fr.n7.stl.tam.ast.Register, int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		thenBranch.allocateMemory(_register, _offset);
		if (elseBranch != null) {
			elseBranch.allocateMemory(_register, _offset);
		}
		return _offset;
		
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		
		Fragment fragment =  _factory.createFragment();
		fragment.append(this.condition.getCode(_factory));
		
		fragment.add(_factory.createJumpIf( "" + _factory.createLabelNumber(), 0));
		fragment.append(this.thenBranch.getCode(_factory));
		
		fragment.add(_factory.createJump( "" + _factory.createLabelNumber()));
		fragment.append(this.elseBranch.getCode(_factory));
		
		return fragment;
	}

}
