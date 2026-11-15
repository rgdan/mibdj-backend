package teccr.mibdj.service.external.taskgenerator;

import teccr.mibdj.data.Task;

/**
 * Interfaz para generar tareas
 */
public interface TaskGenerator {

    /**
     * Genera y devuelve un objeto Task. No recibe parámetros.
     */
    Task generateTask();
}
