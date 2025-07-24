package com.wyldsoft.notes.core.commands

/**
 * Executes commands and manages undo/redo stacks.
 */
interface CommandExecutor {
    /**
     * Executes a command and adds it to the undo stack.
     */
    fun execute(command: Command)
    
    /**
     * Undoes the last command.
     * @return true if undo was successful, false if nothing to undo
     */
    fun undo(): Boolean
    
    /**
     * Redoes the last undone command.
     * @return true if redo was successful, false if nothing to redo
     */
    fun redo(): Boolean
    
    /**
     * Checks if undo is available.
     */
    fun canUndo(): Boolean
    
    /**
     * Checks if redo is available.
     */
    fun canRedo(): Boolean
    
    /**
     * Clears all command history.
     */
    fun clearHistory()
    
    /**
     * Gets the number of commands that can be undone.
     */
    fun getUndoCount(): Int
    
    /**
     * Gets the number of commands that can be redone.
     */
    fun getRedoCount(): Int
    
    /**
     * Sets the maximum size of the undo stack.
     */
    fun setMaxHistorySize(size: Int)
}