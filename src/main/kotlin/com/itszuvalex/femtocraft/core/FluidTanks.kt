package com.itszuvalex.femtocraft.core

import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.transfer.fluid.FluidResource
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler

/**
 * A set of fluid tanks, one per index, with their own capacities, exposed as a NeoForge fluid `ResourceHandler`.
 * Replaces 1.7.10's `FluidTank`/`TileFluidTank`/`TileMultiFluidTank`. Persisted by NeoForge's `StacksResourceHandler`
 * format (key `stacks`).
 *
 * @param onChanged Run after every committed change, e.g. the block entity's `markDirtyAndSync`.
 * @param validator Which fluids a tank index accepts.
 */
open class FluidTanks(
    private val capacities: IntArray,
    private val onChanged: () -> Unit = {},
    private val validator: (Int, FluidResource) -> Boolean = { _, _ -> true },
) : FluidStacksResourceHandler(capacities.size, 0) {

    override fun getCapacity(index: Int, resource: FluidResource): Int = capacities[index]

    override fun isValid(index: Int, resource: FluidResource): Boolean = resource.isEmpty || validator(index, resource)

    override fun onContentsChanged(index: Int, previousContents: FluidStack) = onChanged()

    fun amount(index: Int): Int = getAmountAsInt(index)

    fun capacity(index: Int): Int = capacities[index]

    fun stack(index: Int): FluidStack = stacks[index].copy()

    /**
     * Replaces a tank's contents outside of a transaction (e.g. tick logic, client sync).
     */
    fun setStack(index: Int, stack: FluidStack) {
        stacks[index] = stack.copy()
        onChanged()
    }

    /**
     * Drains up to [amount] from [index] outside of a transaction.
     *
     * @return Amount drained.
     */
    fun drainDirect(index: Int, amount: Int): Int {
        val current = stacks[index]
        if (current.isEmpty || amount <= 0) return 0
        val drained = minOf(amount, current.amount)
        val copy = current.copy()
        copy.shrink(drained)
        stacks[index] = if (copy.isEmpty) FluidStack.EMPTY else copy
        onChanged()
        return drained
    }

    /**
     * Fills [index] outside of a transaction.
     *
     * @return Amount filled.
     */
    fun fillDirect(index: Int, stack: FluidStack): Int {
        if (stack.isEmpty) return 0
        val resource = FluidResource.of(stack)
        if (!isValid(index, resource)) return 0
        val current = stacks[index]
        if (!current.isEmpty && !FluidStack.isSameFluidSameComponents(current, stack)) return 0
        val filled = minOf(stack.amount, capacities[index] - current.amount)
        if (filled <= 0) return 0
        stacks[index] = stack.copyWithAmount(current.amount + filled)
        onChanged()
        return filled
    }
}
