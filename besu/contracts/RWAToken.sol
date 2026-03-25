// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title RWAToken
 * @dev Represents a fractionalized Real-World Asset (Gold/Silver/Real Estate)
 */
contract RWAToken is ERC20, Ownable {
    
    string public assetType;
    uint256 public totalAssetFractions;

    constructor(
        string memory name, 
        string memory symbol, 
        string memory _assetType,
        uint256 _initialSupply
    ) ERC20(name, symbol) Ownable(msg.sender) {
        assetType = _assetType;
        totalAssetFractions = _initialSupply;
        _mint(msg.sender, _initialSupply * (10 ** decimals()));
    }

    /**
     * @dev Only the engine can transfer tokens between users during settlement.
     * In a production environment, this would be restricted to authorized operators.
     */
    function engineTransfer(address from, address to, uint256 amount) external onlyOwner {
        _transfer(from, to, amount);
    }
}
