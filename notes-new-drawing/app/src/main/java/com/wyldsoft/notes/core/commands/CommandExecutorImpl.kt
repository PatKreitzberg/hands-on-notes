package com.wyldsoft.notes.core.commands

import java.util.Stack

/**
 * Default implementation of CommandExecutor.
 */
class CommandExecutorImpl(
    private var maxHistorySize: Int = 100
) : CommandExecutor {
    
    private val undoStack = Stack<Command>()
    private val redoStack = Stack<Command>()
    
    override fun execute(command: Command) {
        // Check if we can merge with the last command
        if (undoStack.isNotEmpty()) {
            val lastCommand = undoStack.peek()
            if (lastCommand.canMergeWith(command)) {
                lastCommand.mergeWith(command)
                return
            }
        }
        
        // Execute the command
        command.execute()
        
        // Add to undo stack
        undoStack.push(command)
        
        // Clear redo stack when new command is executed
        redoStack.clear()
        
        // Limit undo stack size
        while (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }
    
    override fun undo(): Boolean {
        if (!canUndo()) return false
        
        val command = undoStack.pop()
        command.undo()
        redoStack.push(command)
        
        return true
    }
    
    override fun redo(): Boolean {
        if (!canRedo()) return false
        
        val command = redoStack.pop()
        command.execute()
        undoStack.push(command)
        
        return true
    }
    
    override fun canUndo(): Boolean = undoStack.isNotEmpty()
    
    override fun canRedo(): Boolean = redoStack.isNotEmpty()
    
    override fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }
    
    override fun getUndoCount(): Int = undoStack.size
    
    override fun getRedoCount(): Int = redoStack.size
    
    override fun setMaxHistorySize(size: Int) {
        maxHistorySize = size
        // Trim undo stack if needed
        while (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }
}