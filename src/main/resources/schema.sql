-- Jackpot table
CREATE TABLE IF NOT EXISTS jackpot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    initial_pool_value DECIMAL(19, 2) NOT NULL,
    current_pool_value DECIMAL(19, 2) NOT NULL,
    contribution_type VARCHAR(50) NOT NULL,
    contribution_config VARCHAR(1000),
    reward_type VARCHAR(50) NOT NULL,
    reward_config VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Jackpot Contribution table
CREATE TABLE IF NOT EXISTS jackpot_contribution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    jackpot_id BIGINT NOT NULL,
    stake_amount DECIMAL(19, 2) NOT NULL,
    contribution_amount DECIMAL(19, 2) NOT NULL,
    current_jackpot_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jackpot_id) REFERENCES jackpot(id)
);

-- Jackpot Reward table
CREATE TABLE IF NOT EXISTS jackpot_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    jackpot_id BIGINT NOT NULL,
    jackpot_reward_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jackpot_id) REFERENCES jackpot(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_contribution_jackpot_id ON jackpot_contribution(jackpot_id);
CREATE INDEX IF NOT EXISTS idx_contribution_bet_id ON jackpot_contribution(bet_id);
CREATE INDEX IF NOT EXISTS idx_reward_jackpot_id ON jackpot_reward(jackpot_id);
CREATE INDEX IF NOT EXISTS idx_reward_bet_id ON jackpot_reward(bet_id);
