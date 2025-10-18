-- Insert sample jackpots for testing

-- Fixed contribution (10%), Fixed reward (5% chance)
INSERT INTO jackpot (id, initial_pool_value, current_pool_value, contribution_type, contribution_config, reward_type, reward_config)
VALUES (1, 1000.00, 1000.00, 'FIXED_CONTRIBUTION', '{"percentage": 0.10}', 'FIXED_REWARD', '{"percentage": 0.05}');

-- Variable contribution, Variable reward
INSERT INTO jackpot (id, initial_pool_value, current_pool_value, contribution_type, contribution_config, reward_type, reward_config)
VALUES (2, 5000.00, 5000.00, 'VARIABLE_CONTRIBUTION', '{"basePercentage": 0.15, "poolLimit": 10000.00}', 'VARIABLE_REWARD', '{"poolLimit": 10000.00}');

-- Fixed contribution (5%), Fixed reward (10% chance)
INSERT INTO jackpot (id, initial_pool_value, current_pool_value, contribution_type, contribution_config, reward_type, reward_config)
VALUES (3, 2000.00, 2000.00, 'FIXED_CONTRIBUTION', '{"percentage": 0.05}', 'FIXED_REWARD', '{"percentage": 0.10}');
