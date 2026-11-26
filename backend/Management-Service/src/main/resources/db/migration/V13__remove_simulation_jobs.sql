-- The game engine now executes in-process, so an asynchronous simulation queue is no longer needed.
DROP TABLE simulation_jobs;
