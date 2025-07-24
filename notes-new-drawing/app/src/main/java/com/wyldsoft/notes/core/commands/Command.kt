package com.wyldsoft.notes.core.commands

/**
 * Base interface for all commands in the system.
 * Implements the Command pattern for undo/redo functionality.
 */
interface Command {
    /**
     * Executes the command.
     */
    fun execute()
    
    /**
     * Undoes the command.
     */
    fun undo()
    
    /**
     * Determines if this command can be merged with another command.
     * Used for grouping similar commands (e.g., continuous drawing).
     */
    fun canMergeWith(other: Command): Boolean = false
    
    /**
     * Merges this command with another command.
     * Only called if canMergeWith returns true.
     */
    fun mergeWith(other: Command) {}
    
    /**
     * Gets a description of the command for UI display.
     */
    fun getDescription(): String = ""
}